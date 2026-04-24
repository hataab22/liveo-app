# 🎬 Liveo - Smart Streaming Platform

نظام IPTV متكامل مع Admin Panel ونظام أكواد تفعيل احترافي

---

## 📦 المكونات

```
LiveoApp/
├── app/                   # تطبيق Android
├── backend/               # Backend API (Python Flask)
└── admin-panel/           # لوحة تحكم Admin
```

---

## 🚀 التشغيل السريع

### 1️⃣ Backend (API Server)

```bash
cd backend
pip install flask flask-cors
python app.py
```

✅ السيرفر سيعمل على: `http://localhost:5000`

**تسجيل دخول افتراضي:**
- Username: `admin`
- Password: `admin123`

---

### 2️⃣ Admin Panel

```bash
cd admin-panel
# افتح index.html في المتصفح
```

أو استخدم Live Server في VS Code

---

### 3️⃣ تطبيق Android

1. افتح المجلد `LiveoApp` في Android Studio
2. انتظر Gradle Sync
3. عدّل رابط API في التطبيق:
   ```kotlin
   // في ملف ApiClient.kt
   const val BASE_URL = "http://YOUR_SERVER_IP:5000"
   ```
4. اضغط Run ▶️

---

## 🎨 الهوية البصرية - Liveo

### الألوان:
- **Primary:** `#C850FF` (بنفسجي)
- **Secondary:** `#00D9FF` (سماوي)
- **Background:** `#1A1A2E` (داكن)
- **Cards:** `#2E2E3E` (رمادي داكن)

### الشعار:
- رمز L حديث مع تدرج بنفسجي → سماوي

---

## ✨ المميزات

### للمستخدم:
- ✅ تفعيل بكود بسيط
- ✅ قائمة المفضلة (مع إعادة ترتيب)
- ✅ آخر القنوات المشاهدة
- ✅ بحث وفلترة
- ✅ مشغل فيديو احترافي
- ✅ واجهة Liveo الفريدة

### للآدمن:
- ✅ إنشاء أكواد تفعيل
- ✅ إضافة روابط M3U
- ✅ تحديد مدة الاشتراك
- ✅ إحصائيات مباشرة
- ✅ تفعيل/تعطيل الأكواد
- ✅ حذف الأكواد

---

## 📱 كيفية الاستخدام

### 🎯 أنت (الآدمن):

1. **افتح Admin Panel**
   ```
   http://localhost/admin-panel
   ```

2. **سجّل دخول**
   - Username: admin
   - Password: admin123

3. **أنشئ كود جديد**
   - رابط M3U: `http://provider.com/playlist.m3u`
   - المدة: 30 يوم
   - اسم العميل: محمد (اختياري)
   - اضغط "إنشاء كود"

4. **أعطِ الكود للعميل**
   - مثال: `ABC12345`

---

### 👤 العميل:

1. **يفتح التطبيق**
2. **يدخل الكود:** `ABC12345`
3. **يضغط "تفعيل"**
4. **يشاهد القنوات تلقائياً!** 🎉

---

## 🔑 نظام الأكواد

### إنشاء كود:

```python
POST /admin/create_code
{
  "code": "ABC123",        # اختياري - يُنشأ تلقائياً
  "m3u_url": "http://...",
  "duration_days": 30,
  "customer_name": "محمد"
}
```

### تفعيل كود (من التطبيق):

```python
POST /activate
{
  "code": "ABC123",
  "device_id": "unique_device_id"
}
```

**الرد:**
```json
{
  "success": true,
  "m3u_url": "http://...",
  "expires_at": 1234567890,
  "days_left": 25
}
```

---

## 💾 قاعدة البيانات

### جدول Codes:

| Field | Type | Description |
|-------|------|-------------|
| code | TEXT | كود التفعيل |
| m3u_url | TEXT | رابط M3U |
| duration_days | INT | مدة الاشتراك |
| created_at | INT | تاريخ الإنشاء |
| expires_at | INT | تاريخ الانتهاء |
| is_active | INT | 1=نشط, 0=معطل |
| customer_name | TEXT | اسم العميل |
| device_id | TEXT | معرف الجهاز |

---

## 📊 API Endpoints

### Admin:
- `POST /admin/login` - تسجيل دخول
- `POST /admin/create_code` - إنشاء كود
- `GET /admin/codes` - جميع الأكواد
- `DELETE /admin/delete_code/<code>` - حذف كود
- `POST /admin/toggle_code/<code>` - تفعيل/تعطيل

### App:
- `POST /activate` - تفعيل كود

---

## 🎯 السيناريو الكامل

### مثال واقعي:

```
📍 اليوم 1:
أنت: تنشئ كود "LIVE2025" لمدة 30 يوم
      رابط M3U من مزودك
      
العميل: يدخل "LIVE2025" في التطبيق
         يحصل على القنوات فوراً

📍 اليوم 15:
العميل: يضيف قنوات للمفضلة
         يرتبها حسب تفضيله
         
📍 اليوم 30:
النظام: الكود ينتهي تلقائياً
أنت: تنشئ كود جديد للتجديد
```

---

## 🔮 المستقبل (Reseller Panel)

عندما تشتري Reseller Panel:

```python
# فقط أضف هذا في create_code:
def create_xtream_account(duration):
    # اتصال بـ Xtream API
    response = xtream_api.create_user(
        username="user_" + code,
        password=random_password(),
        duration=duration
    )
    return response
```

**بدون تغيير التطبيق!** ✅

---

## 🛠️ المتطلبات

### Backend:
```
Python 3.8+
Flask
Flask-CORS
```

### Android App:
```
Android Studio Arctic Fox+
Min SDK: 24 (Android 7.0)
Target SDK: 34
```

---

## 💰 نموذج الربح

```
تكاليف:
- Backend Hosting: 5$ شهرياً
- Reseller Panel: 100$ شهرياً (لاحقاً)

إيرادات:
- تبيع اشتراك: 5$ شهرياً
- 80 عميل: 400$ شهرياً
- ربح: 295$ شهرياً 🎉
```

---

## 🔐 الأمان

- ✅ تشفير كلمات المرور (SHA-256)
- ✅ ربط الكود بجهاز واحد
- ✅ فحص تاريخ الانتهاء تلقائياً
- ✅ إمكانية تعطيل الأكواد

---

## 📱 المميزات القادمة

- [ ] VOD (أفلام ومسلسلات)
- [ ] EPG (جدول البرامج)
- [ ] Push Notifications
- [ ] نظام الدفع المدمج
- [ ] تقارير مفصلة
- [ ] دعم متعدد اللغات

---

## 🎉 الخلاصة

لديك الآن نظام **Liveo** كامل:

✅ Admin Panel احترافي
✅ Backend API قوي  
✅ تطبيق Android جميل
✅ نظام أكواد تفعيل
✅ المفضلة وسجل المشاهدة

**جاهز للانطلاق!** 🚀

---

## 📞 الدعم

أي استفسار أو مشكلة؟ أنا هنا! 😊
