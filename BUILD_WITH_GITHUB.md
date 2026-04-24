# 🤖 بناء APK تلقائياً عبر GitHub Actions

---

## ✅ الطريقة الأسهل - GitHub يبنيه لك!

بدل ما تبني على جهازك، GitHub يبنيه لك مجاناً!

---

## 📋 الخطوات (10 دقائق):

### 1️⃣ ارفع المشروع على GitHub

1. **في GitHub.com**، أنشئ Repository جديد:
   ```
   اسم: liveo-app
   Public ✅
   ```

2. **ارفع مجلد LiveoApp** كامل

---

### 2️⃣ أنشئ ملف GitHub Workflow

3. في Repository، اضغط **Add file** → **Create new file**

4. **اسم الملف:**
   ```
   .github/workflows/build.yml
   ```

5. **المحتوى (انسخ والصق):**

```yaml
name: Build APK

on:
  push:
    branches: [ main ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Build APK
      run: ./gradlew assembleDebug
    
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
```

6. **اضغط Commit changes**

---

### 3️⃣ تحميل APK

7. **اذهب لـ Actions** (في أعلى Repository)

8. **انتظر البناء** (3-5 دقائق)

9. **لما يخلص:**
   - ستجد **app-debug** في Artifacts
   - **اضغط عليه لتحميل APK!**

✅ **APK جاهز للتحميل!**

---

## 🎯 الميزة:

- ✅ بناء تلقائي
- ✅ بدون مشاكل Java
- ✅ APK جاهز للتحميل
- ✅ مجاني تماماً!

---

## 💡 ملاحظة:

**الملفات محدثة بالفعل:**
- ApiClient.kt ← رابط Render موجود ✅
- admin-panel/index.html ← رابط Render موجود ✅

**فقط ارفع المشروع على GitHub واتبع الخطوات!**

---

🚀 **في 10 دقائق، APK جاهز!**
