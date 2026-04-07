import os

css_path = r'd:\JavaUdemy\kids-fashion-shop\src\main\resources\static\css\app.css'

with open(css_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Reset Header Wrapper (Remove messy important/mixed styles)
content = content.replace(
    'background: rgba(255, 255, 255, 0.72) !important; backdrop-filter: blur(20px) !important; -webkit-backdrop-filter: blur(20px) !important; border-bottom: 1px solid rgba(0, 0, 0, 0.06) !important;',
    'background: rgba(255, 255, 255, 0.8) !important; backdrop-filter: blur(25px) !important; -webkit-backdrop-filter: blur(25px) !important; border-bottom: 1px solid rgba(0, 0, 0, 0.04) !important;'
)

# 2. Fix Dark Mode Header Blob
content = content.replace(
    '[data-theme="dark"] .header-wrapper {\n  background: rgba(18, 18, 18, 0.85);\n}',
    '[data-theme="dark"] .header-wrapper {\n  background: rgba(18, 18, 18, 0.8) !important;\n  border-bottom-color: rgba(255, 255, 255, 0.08) !important;\n}'
)

# 3. Clean up Header Main (Remove solid background)
content = content.replace('.header-main {\n  background: #fff;\n}', '.header-main {\n  background: transparent !important;\n}')

# 4. Redesign Search Form (Boutique Minimalism)
search_old = """.search-form {
  display: flex;
  align-items: center;
  gap: 2px;
  /* KhÃ´ng dÃ¹ng vw quÃ¡ lá»›n â€” trÃ¡nh Ã´ tÃ¬m kiáº¿m Ä‘Ã¨ lÃªn menu á»Ÿ laptop */
  width: auto;
  max-width: 260px; /* Tăng lại chiều ngang tối đa */
  min-width: 0;
  flex: 1 1 140px; /* Cho phép giãn ra để lấp đầy khoảng trống */
  min-height: 44px;
  padding: 0 6px 0 14px;
  background: linear-gradient(180deg, #ffffff 0%, #fafafa 100%);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 999px;
  box-shadow:
    0 1px 2px rgba(0, 0, 0, 0.04),
    0 4px 14px rgba(0, 0, 0, 0.05);
  transition: border-color 0.22s ease, box-shadow 0.22s ease, background 0.22s ease;
}"""

search_new = """.search-form {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  max-width: 320px;
  min-width: 0;
  flex: 1 1 auto;
  min-height: 42px;
  padding: 0 4px 0 16px;
  background: rgba(0, 0, 0, 0.035) !important;
  border: 1px solid transparent !important;
  border-radius: 999px;
  transition: all 0.25s ease;
}
[data-theme="dark"] .search-form {
  background: rgba(255, 255, 255, 0.06) !important;
}"""

# Try to find the search block even if whitespace differs slightly
if search_old in content:
    content = content.replace(search_old, search_new)
else:
    # Fallback to a simpler replace if exact match fails
    content = content.replace('background: linear-gradient(180deg, #ffffff 0%, #fafafa 100%);', 'background: rgba(0, 0, 0, 0.035) !important;')
    content = content.replace('max-width: 260px; /* Tăng lại chiều ngang tối đa */', 'max-width: 320px;')

with open(css_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Đã hoàn thành nâng cấp Header Boutique V2!")
