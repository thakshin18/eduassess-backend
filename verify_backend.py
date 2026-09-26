import requests
import json
import uuid

API_BASE = "http://localhost:8080/api"

ADMIN_EMAIL = "admin@example.com"
ADMIN_PASSWORD = "admin123"
STUDENT_EMAIL = "student@example.com"
STUDENT_PASSWORD = "student123"

report = {}

def assert_status(response, expected_status, test_name):
    if type(expected_status) == list:
        if response.status_code in expected_status:
            report[test_name] = "PASS"
        else:
            report[test_name] = f"FAIL (Expected {expected_status}, got {response.status_code})"
    else:
        if response.status_code == expected_status:
            report[test_name] = "PASS"
        else:
            report[test_name] = f"FAIL (Expected {expected_status}, got {response.status_code})"

def login(email, password):
    r = requests.post(f"{API_BASE}/auth/login", json={"email": email, "password": password})
    if r.status_code == 200:
        return r.json()["token"]
    return None

admin_token = login(ADMIN_EMAIL, ADMIN_PASSWORD)
if admin_token: report["Admin login"] = "PASS"
else: report["Admin login"] = "FAIL"

student_token = login(STUDENT_EMAIL, STUDENT_PASSWORD)
if student_token: report["Student login"] = "PASS"
else: report["Student login"] = "FAIL"

r_invalid = requests.post(f"{API_BASE}/auth/login", json={"email": "admin@example.com", "password": "wrong"})
assert_status(r_invalid, 401, "Invalid login rejection")

admin_headers = {"Authorization": f"Bearer {admin_token}"}
r_tests = requests.get(f"{API_BASE}/tests", headers=admin_headers)
if r_tests.status_code == 200:
    report["Admin authorization"] = "PASS"
else:
    report["Admin authorization"] = "FAIL"

new_test = {
    "title": "E2E Created Test",
    "description": "desc",
    "subject": "Math",
    "difficulty": "Hard",
    "duration": 60,
    "totalMarks": 10,
    "passingMarks": 5,
    "status": "DRAFT"
}
r_create_test = requests.post(f"{API_BASE}/tests", json=new_test, headers=admin_headers)

if r_create_test.status_code in [200, 201]:
    test_id = r_create_test.json()["id"]
    r_pub = requests.put(f"{API_BASE}/tests/{test_id}/publish", headers=admin_headers)

student_headers = {"Authorization": f"Bearer {student_token}"}
r_pub_tests = requests.get(f"{API_BASE}/tests/published", headers=student_headers)
r_create_student = requests.post(f"{API_BASE}/tests", json=new_test, headers=student_headers)
if r_create_student.status_code == 403:
    report["Student restrictions"] = "PASS"
else:
    report["Student restrictions"] = "FAIL"

published_tests = r_pub_tests.json()
if len(published_tests) > 0:
    published_test_id = published_tests[0]["id"]
    r_take = requests.get(f"{API_BASE}/tests/{published_test_id}", headers=student_headers)
    if r_take.status_code == 200:
        test_data = r_take.json()
        questions = test_data.get("questions", [])
        if len(questions) > 0:
            leak = False
            for q in questions:
                if "isCorrect" in q or "correctOptionId" in q or "correctAnswer" in q or "answerKey" in q:
                    leak = True
                for opt in q.get("options", []):
                    if "isCorrect" in opt:
                        leak = True
            report["Answer leakage"] = "FAIL" if leak else "PASS"
            
            q1 = questions[0]
            o1 = q1["options"][0]
            submission = {
                "answers": [
                    {"questionId": q1["id"], "optionId": o1["id"]}
                ]
            }
            r_submit = requests.post(f"{API_BASE}/tests/{published_test_id}/submit", json=submission, headers=student_headers)
            assert_status(r_submit, 200, "Submission validation")
            
            if r_submit.status_code == 200:
                sub_res = r_submit.json()
                if "totalMarks" in sub_res and "percentage" in sub_res and "correctAnswers" in sub_res and "score" in sub_res:
                    report["Score authority"] = "PASS"
                    result_id = sub_res["id"]
                    
                    r_my_res = requests.get(f"{API_BASE}/results/my-results", headers=student_headers)
                    assert_status(r_my_res, 200, "Result ownership")
                    
                    r_del = requests.delete(f"{API_BASE}/results/{result_id}", headers=student_headers)
                    assert_status(r_del, [403, 404, 405], "Result immutability")
                else:
                    report["Score authority"] = "FAIL (Missing score fields)"
            
r_401 = requests.get(f"{API_BASE}/tests", headers={"Authorization": "Bearer invalidtoken"})
assert_status(r_401, 401, "401 handling")
report["403 handling"] = report.get("Student restrictions", "FAIL")

for k, v in report.items():
    print(f"{k}: {v}")
