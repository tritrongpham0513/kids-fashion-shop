package com.kidfashion.ecommerce.kids_fashion_shop.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Khóa dòng giỏ: productId|màu|size — chỉ dùng ký tự ASCII (an toàn cho form POST / HTML).
 */
public final class CartLineKey {

	private static final char LEGACY_SEP = '\u001e';

	private CartLineKey() {
	}

	public static String encode(long productId, String colorLabel, String sizeLabel) {
		return productId + "|" + norm(colorLabel) + "|" + norm(sizeLabel);
	}

	/**
	 * Hỗ trợ: (1) định dạng mới "id|màu|size", (2) phiên bản cũ có ký tự RS, (3) chỉ số id.
	 */
	public static Parsed parse(String key) {
		if (key == null || key.isBlank()) {
			throw new IllegalArgumentException("lineKey rỗng");
		}
		String k = key.trim();
		if (k.indexOf(LEGACY_SEP) >= 0) {
			int i = k.indexOf(LEGACY_SEP);
			int j = k.indexOf(LEGACY_SEP, i + 1);
			if (i < 0 || j < 0) {
				return new Parsed(Long.parseLong(k), "", "");
			}
			long pid = Long.parseLong(k.substring(0, i).trim());
			String color = k.substring(i + 1, j);
			String size = k.substring(j + 1);
			return new Parsed(pid, color, size);
		}
		if (!k.contains("|")) {
			return new Parsed(Long.parseLong(k), "", "");
		}
		String[] parts = k.split("\\|", 3);
		if (parts.length != 3) {
			throw new IllegalArgumentException("lineKey không hợp lệ");
		}
		return new Parsed(Long.parseLong(parts[0].trim()), parts[1], parts[2]);
	}

	private static String norm(String s) {
		if (s == null) {
			return "";
		}
		String t = s.trim();
		if (t.length() > 120) {
			t = t.substring(0, 120);
		}
		return t.replace("|", " ").replace(LEGACY_SEP, ' ');
	}

	/** Đổi khóa cũ (có RS) trong session sang định dạng mới. */
	public static void rekeyLegacyRows(Map<String, Integer> map) {
		if (map == null || map.isEmpty()) {
			return;
		}
		List<String> oldKeys = new ArrayList<>();
		for (String k : map.keySet()) {
			if (k != null && k.indexOf(LEGACY_SEP) >= 0) {
				oldKeys.add(k);
			}
		}
		for (String k : oldKeys) {
			Integer q = map.remove(k);
			if (q == null) {
				continue;
			}
			try {
				Parsed p = parse(k);
				String nk = encode(p.productId, p.colorLabel, p.sizeLabel);
				map.merge(nk, q, Integer::sum);
			} catch (Exception ignored) {
				// bỏ qua
			}
		}
	}

	public static final class Parsed {
		public final long productId;
		public final String colorLabel;
		public final String sizeLabel;

		public Parsed(long productId, String colorLabel, String sizeLabel) {
			this.productId = productId;
			this.colorLabel = colorLabel == null ? "" : colorLabel;
			this.sizeLabel = sizeLabel == null ? "" : sizeLabel;
		}
	}
}
