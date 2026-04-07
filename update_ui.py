import os

css_path = r'd:\JavaUdemy\kids-fashion-shop\src\main\resources\static\css\app.css'

with open(css_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Tăng chiều cao Hero
content = content.replace('height: clamp(460px, 52vw, 620px);', 'height: clamp(520px, 58vw, 750px);')

# 2. Làm rõ ảnh sản phẩm (Giảm overlay)
content = content.replace(
    'background: linear-gradient(to right, rgba(0, 0, 0, 0.6) 0%, rgba(0, 0, 0, 0.2) 50%, rgba(0, 0, 0, 0.4) 100%);',
    'background: linear-gradient(to bottom, rgba(0, 0, 0, 0.05) 0%, rgba(0, 0, 0, 0.35) 100%);'
)

# 3. Tăng độ rõ nét và hiệu ứng cho ảnh Hero
content = content.replace(
    'transform: scale(1.1);\n  transition: transform 8s ease;',
    'transform: scale(1.02);\n  transition: transform 15s cubic-bezier(0.25, 0.46, 0.45, 0.94);\n  filter: brightness(1.08) contrast(1.05);'
)
content = content.replace('transform: scale(1);', 'transform: scale(1.12);')

# 4. Header Glassmorphism
content = content.replace(
    'background: #fff;\n  border-bottom: 1px solid #e8e8e8;',
    'background: rgba(255, 255, 255, 0.72) !important; backdrop-filter: blur(20px) !important; -webkit-backdrop-filter: blur(20px) !important; border-bottom: 1px solid rgba(0, 0, 0, 0.06) !important;'
)

# 5. Hiệu ứng nhấc bổng cho Card sản phẩm
content = content.replace(
    '.card-product {\n  display: flex;',
    '.card-product { transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1); display: flex;'
)
content = content.replace(
    '.card-product:hover .card-actions {',
    '.card-product:hover { transform: translateY(-8px); box-shadow: 0 20px 40px rgba(0,0,0,0.08); } .card-product:hover .card-actions {'
)

with open(css_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Đã cập nhật giao diện Luxury Boutique thành công!")
