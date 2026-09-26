const API_BASE = "http://localhost:8080/api";
const ADMIN_EMAIL = "admin@example.com";
const ADMIN_PASSWORD = "admin123";
const STUDENT_EMAIL = "student@example.com";
const STUDENT_PASSWORD = "student123";

const report = {};

function assertStatus(status, expected, testName) {
    if (Array.isArray(expected)) {
        if (expected.includes(status)) {
            report[testName] = "PASS";
        } else {
            report[testName] = `FAIL (Expected ${expected}, got ${status})`;
        }
    } else {
        if (status === expected) {
            report[testName] = "PASS";
        } else {
            report[testName] = `FAIL (Expected ${expected}, got ${status})`;
        }
    }
}

async function login(email, password) {
    const r = await fetch(`${API_BASE}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password })
    });
    if (r.status === 200) {
        const data = await r.json();
        return data.token;
    }
    return null;
}

async function runTests() {
    const adminToken = await login(ADMIN_EMAIL, ADMIN_PASSWORD);
    report["Admin login"] = adminToken ? "PASS" : "FAIL";
    
    const studentToken = await login(STUDENT_EMAIL, STUDENT_PASSWORD);
    report["Student login"] = studentToken ? "PASS" : "FAIL";
    
    const rInvalid = await fetch(`${API_BASE}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: ADMIN_EMAIL, password: "wrong" })
    });
    assertStatus(rInvalid.status, 401, "Invalid login rejection");
    
    const adminHeaders = { "Authorization": `Bearer ${adminToken}`, "Content-Type": "application/json" };
    const rTests = await fetch(`${API_BASE}/tests`, { headers: adminHeaders });
    report["Admin authorization"] = rTests.status === 200 ? "PASS" : "FAIL";
    
    const newTest = {
        title: "E2E Created Test",
        description: "desc",
        subject: "Math",
        difficulty: "Hard",
        duration: 60,
        totalMarks: 10,
        passingMarks: 5,
        status: "DRAFT"
    };
    
    const rCreateTest = await fetch(`${API_BASE}/tests`, {
        method: "POST",
        headers: adminHeaders,
        body: JSON.stringify(newTest)
    });
    
    if ([200, 201].includes(rCreateTest.status)) {
        const testData = await rCreateTest.json();
        const rPub = await fetch(`${API_BASE}/tests/${testData.id}/publish`, {
            method: "PATCH",
            headers: adminHeaders
        });
    }
    
    const studentHeaders = { "Authorization": `Bearer ${studentToken}`, "Content-Type": "application/json" };
    const rPubTests = await fetch(`${API_BASE}/tests`, { headers: studentHeaders });
    
    const rCreateStudent = await fetch(`${API_BASE}/tests`, {
        method: "POST",
        headers: studentHeaders,
        body: JSON.stringify(newTest)
    });
    assertStatus(rCreateStudent.status, 403, "Student restrictions");
    
    if (rPubTests.status === 200) {
        const publishedTests = await rPubTests.json();
        let pubTestId = null;
        for (const pt of publishedTests) {
            const rTakePt = await fetch(`${API_BASE}/tests/${pt.id}/questions`, { headers: studentHeaders });
            if (rTakePt.status === 200) {
                const questionsList = await rTakePt.json();
                if (questionsList && questionsList.length > 0) {
                    pubTestId = pt.id;
                    break;
                }
            }
        }
        
        if (pubTestId) {
            const rTake = await fetch(`${API_BASE}/tests/${pubTestId}`, { headers: studentHeaders });
            const rQuestions = await fetch(`${API_BASE}/tests/${pubTestId}/questions`, { headers: studentHeaders });
            
            if (rTake.status === 200 && rQuestions.status === 200) {
                const questions = await rQuestions.json();
                if (questions.length > 0) {
                    let leak = false;
                    for (const q of questions) {
                        if (q.isCorrect !== undefined || q.correctOptionId !== undefined || q.correctAnswer !== undefined || q.answerKey !== undefined) {
                            leak = true;
                        }
                        for (const opt of q.options || []) {
                            if (opt.isCorrect !== undefined) leak = true;
                        }
                    }
                    report["Answer leakage"] = leak ? "FAIL" : "PASS";
                    
                    const q1 = questions[0];
                    const o1 = q1.options && q1.options.length > 0 ? q1.options[0] : null;
                    if (o1) {
                        const submission = {
                            answers: [ { questionId: q1.id, optionId: o1.id } ]
                        };
                        
                        const rSubmit = await fetch(`${API_BASE}/tests/${pubTestId}/submit`, {
                            method: "POST",
                            headers: studentHeaders,
                            body: JSON.stringify(submission)
                        });
                        assertStatus(rSubmit.status, 200, "Submission validation");
                        
                        if (rSubmit.status === 200) {
                            const subRes = await rSubmit.json();
                            if (subRes.totalMarks !== undefined && subRes.percentage !== undefined && subRes.correctAnswers !== undefined && subRes.score !== undefined) {
                                report["Score authority"] = "PASS";
                                const resId = subRes.id;
                                
                                const rMyRes = await fetch(`${API_BASE}/results/my`, { headers: studentHeaders });
                                assertStatus(rMyRes.status, 200, "Result ownership");
                                
                                const rDel = await fetch(`${API_BASE}/results/${resId}`, { method: "DELETE", headers: studentHeaders });
                                assertStatus(rDel.status, [403, 404, 405], "Result immutability");
                            } else {
                                report["Score authority"] = "FAIL (Missing score fields)";
                            }
                        }
                    } else {
                        report["Answer leakage"] = "FAIL (No options found)";
                    }
                } else {
                    report["Answer leakage"] = "FAIL (No questions in fetched test)";
                }
            } else {
                console.log("Fetch test status:", rTake.status);
            }
        } else {
             console.log("No pubTestId found with questions");
        }
    }
    
    const r401 = await fetch(`${API_BASE}/tests`, { headers: { "Authorization": "Bearer invalid" } });
    assertStatus(r401.status, 401, "401 handling");
    report["403 handling"] = report["Student restrictions"];
    
    for (const [k, v] of Object.entries(report)) {
        console.log(`${k}: ${v}`);
    }
}

runTests().catch(console.error);
