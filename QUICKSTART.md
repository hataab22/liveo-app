# 🚀 البدء السريع - Liveo

## ⚡ 3 خطوات فقط!

### 1️⃣ شغّل Backend

```bash
cd backend
pip install -r requirements.txt
python app.py
```

✅ سيعمل على: http://localhost:5000

---

### 2️⃣ افتح Admin Panel

افتح في المتصفح:
```
admin-panel/index.html
```

**تسجيل دخول:**
- Username: `admin`
- Password: `admin123`

---

### 3️⃣ أنشئ أول كود

في Admin Panel:
1. أدخل رابط M3U
2. اختر المدة (30 يوم)
3. اضغط "إنشاء كود"
4. ستحصل على كود مثل: `LIVE2025`

---

## 📱 التطبيق

**ملاحظة:** التطبيق مش مكتمل بعد لأنه يحتاج ملفات كثيرة.

**لكن النظام الأساسي جاهز:**
- ✅ Backend API
- ✅ Admin Panel
- ✅ نظام الأكواد

**الخطوة القادمة:**
أكمل ملفات التطبيق أو استخدم النظام مع أي تطبيق IPTV آخر!

---

## 🎯 اختبار النظام

### اختبار API:

```bash
# إنشاء كود
curl -X POST http://localhost:5000/admin/create_code \
  -H "Content-Type: application/json" \
  -d '{"m3u_url":"http://test.com/playlist.m3u","duration_days":30}'

# تفعيل كود
curl -X POST http://localhost:5000/activate \
  -H "Content-Type: application/json" \
  -d '{"code":"ABC12345","device_id":"test_device"}'
```

---

## 💡 نصائح

1. **غيّر كلمة مرور الآدمن** في `backend/app.py`
2. **استخدم HTTPS** في Production
3. **احفظ نسخة احتياطية** من `liveo.db`

---

🎉 **مبروك! نظام Liveo جاهز!**
