from flask import Flask, request, jsonify, send_from_directory
from flask_cors import CORS
import sqlite3
import hashlib
import time
import random
import string
import os

app = Flask(__name__)
CORS(app)

DATABASE = 'liveo.db'

def init_db():
    conn = sqlite3.connect(DATABASE)
    c = conn.cursor()
    
    c.execute('''CREATE TABLE IF NOT EXISTS codes
                 (code TEXT PRIMARY KEY,
                  m3u_url TEXT,
                  duration_days INTEGER,
                  created_at INTEGER,
                  expires_at INTEGER,
                  is_active INTEGER,
                  customer_name TEXT,
                  device_id TEXT,
                  parental_pin TEXT)''')
    
    # إضافة parental_pin للجداول القديمة
    try:
        c.execute('ALTER TABLE codes ADD COLUMN parental_pin TEXT')
    except sqlite3.OperationalError:
        pass  # العمود موجود بالفعل
    
    c.execute('''CREATE TABLE IF NOT EXISTS admin
                 (username TEXT PRIMARY KEY,
                  password_hash TEXT)''')
    
    admin_exists = c.execute('SELECT * FROM admin WHERE username=?', ('admin',)).fetchone()
    if not admin_exists:
        password_hash = hashlib.sha256('admin123'.encode()).hexdigest()
        c.execute('INSERT INTO admin VALUES (?, ?)', ('admin', password_hash))
    
    conn.commit()
    conn.close()

def generate_code():
    return ''.join(random.choices(string.ascii_uppercase + string.digits, k=8))

@app.route('/')
def index():
    return jsonify({'app': 'Liveo Backend', 'status': 'running', 'version': '1.0'})

@app.route('/admin/login', methods=['POST'])
def admin_login():
    data = request.json
    username = data.get('username')
    password = data.get('password')
    
    password_hash = hashlib.sha256(password.encode()).hexdigest()
    
    conn = sqlite3.connect(DATABASE)
    c = conn.cursor()
    result = c.execute('SELECT * FROM admin WHERE username=? AND password_hash=?', 
                      (username, password_hash)).fetchone()
    conn.close()
    
    return jsonify({'success': result is not None})

@app.route('/admin/create_code', methods=['POST'])
def create_code():
    data = request.json
    code = data.get('code') or generate_code()
    m3u_url = data.get('m3u_url', '')
    duration_days = int(data.get('duration_days', 30))
    customer_name = data.get('customer_name', '')
    parental_pin = data.get('parental_pin', '')  # 🆕
    
    created_at = int(time.time())
    expires_at = created_at + (duration_days * 24 * 60 * 60)
    
    conn = sqlite3.connect(DATABASE)
    c = conn.cursor()
    
    try:
        c.execute('''INSERT INTO codes VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)''',
                  (code, m3u_url, duration_days, created_at, expires_at, 1, customer_name, '', parental_pin))
        conn.commit()
        return jsonify({
            'success': True, 
            'code': code,
            'parental_pin': parental_pin if parental_pin else None
        })
    except sqlite3.IntegrityError:
        return jsonify({'success': False, 'message': 'الكود موجود مسبقاً'})
    finally:
        conn.close()

@app.route('/admin/codes', methods=['GET'])
def get_codes():
    conn = sqlite3.connect(DATABASE)
    c = conn.cursor()
    codes = c.execute('SELECT * FROM codes ORDER BY created_at DESC').fetchall()
    conn.close()
    
    result = []
    for code in codes:
        result.append({
            'code': code[0],
            'm3u_url': code[1],
            'duration_days': code[2],
            'created_at': code[3],
            'expires_at': code[4],
            'is_active': code[5],
            'customer_name': code[6],
            'device_id': code[7]
        })
    
    return jsonify(result)

@app.route('/activate', methods=['POST'])
def activate():
    data = request.json
    code = data.get('code', '').upper()
    device_id = data.get('device_id', '')
    
    conn = sqlite3.connect(DATABASE)
    c = conn.cursor()
    
    result = c.execute('SELECT * FROM codes WHERE code=?', (code,)).fetchone()
    
    if not result:
        conn.close()
        return jsonify({'success': False, 'message': 'كود غير صحيح'})
    
    current_time = int(time.time())
    
    # التحقق من الصلاحية
    if result[4] < current_time:
        conn.close()
        return jsonify({'success': False, 'message': 'انتهت صلاحية الكود'})
    
    if result[5] == 0:
        conn.close()
        return jsonify({'success': False, 'message': 'الكود معطل'})
    
    # التحقق من الجهاز
    stored_device_id = result[7]
    if stored_device_id and stored_device_id != device_id:
        conn.close()
        return jsonify({'success': False, 'message': 'الكود مستخدم على جهاز آخر'})
    
    # حفظ معرف الجهاز
    if not stored_device_id:
        c.execute('UPDATE codes SET device_id=? WHERE code=?', (device_id, code))
        conn.commit()
    
    conn.close()
    
    return jsonify({
        'success': True,
        'code': result[0],
        'm3u_url': result[1],
        'expires_at': result[4],
        'customer_name': result[6],
        'days_left': (result[4] - current_time) // (24 * 60 * 60),
        'parental_pin': result[8] if len(result) > 8 else None  # 🆕
    })

@app.route('/admin/delete_code/<code>', methods=['DELETE'])
def delete_code(code):
    conn = sqlite3.connect(DATABASE)
    c = conn.cursor()
    c.execute('DELETE FROM codes WHERE code=?', (code,))
    conn.commit()
    conn.close()
    return jsonify({'success': True})

@app.route('/admin/toggle_code/<code>', methods=['POST'])
def toggle_code(code):
    conn = sqlite3.connect(DATABASE)
    c = conn.cursor()
    current = c.execute('SELECT is_active FROM codes WHERE code=?', (code,)).fetchone()
    if current:
        new_status = 0 if current[0] == 1 else 1
        c.execute('UPDATE codes SET is_active=? WHERE code=?', (new_status, code))
        conn.commit()
    conn.close()
    return jsonify({'success': True})

@app.route('/admin/reset_code/<code>', methods=['POST'])
def reset_code(code):
    """إعادة تعيين الكود (مسح device_id) للسماح بجهاز جديد"""
    conn = sqlite3.connect(DATABASE)
    c = conn.cursor()
    c.execute('UPDATE codes SET device_id=NULL WHERE code=?', (code,))
    conn.commit()
    conn.close()
    return jsonify({'success': True, 'message': 'تم إعادة تعيين الكود بنجاح'})

@app.route('/verify_pin', methods=['POST'])
def verify_pin():
    """التحقق من الرقم السري للمحتوى البالغ"""
    data = request.json
    code = data.get('code', '').upper()
    pin = data.get('pin', '')
    
    conn = sqlite3.connect(DATABASE)
    c = conn.cursor()
    
    result = c.execute('SELECT parental_pin FROM codes WHERE code=?', (code,)).fetchone()
    conn.close()
    
    if not result:
        return jsonify({'success': False, 'message': 'كود غير موجود'})
    
    stored_pin = result[0]
    
    # إذا لم يتم تعيين رقم سري
    if not stored_pin or stored_pin == '':
        return jsonify({
            'success': True,
            'valid': False,
            'message': 'الحماية غير مفعلة لهذا الحساب'
        })
    
    # التحقق من الرقم
    is_valid = (stored_pin == pin)
    
    return jsonify({
        'success': True,
        'valid': is_valid,
        'message': 'صحيح' if is_valid else 'رقم سري خاطئ'
    })

@app.route('/admin-panel/index.html')
def admin_panel():
    """صفحة لوحة التحكم"""
    return send_from_directory('admin-panel', 'index.html')

if __name__ == '__main__':
    init_db()
    port = int(os.environ.get('PORT', 10000))
    app.run(debug=False, host='0.0.0.0', port=port)
