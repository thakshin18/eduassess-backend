const API_BASE = 'http://localhost:8080/api';

async function run() {
    try {
        console.log("Verifying seed for Alice...");
        const loginRes = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: 'alice@example.com', password: 'student123' })
        });
        const loginData = await loginRes.json();
        
        if (!loginData.token) {
            console.log("Login failed");
            return;
        }
        console.log("Login success! Token received.");

        console.log("Fetching tests...");
        const testsRes = await fetch(`${API_BASE}/tests`, {
            headers: { 'Authorization': `Bearer ${loginData.token}` }
        });
        const tests = await testsRes.json();
        console.log(`Found ${tests.length} tests.`);
        tests.forEach(t => console.log(` - ${t.title} (${t.status})`));

        console.log("Fetching Alice's results...");
        const resultsRes = await fetch(`${API_BASE}/results/my-results`, {
            headers: { 'Authorization': `Bearer ${loginData.token}` }
        });
        const results = await resultsRes.json();
        console.log(`Found ${results.length} results.`);
        results.forEach(r => console.log(` - Score: ${r.score}/${r.totalMarks} (${r.percentage}%) for Test ID: ${r.testId}`));

    } catch (e) {
        console.error("Error:", e.message);
    }
}
run();
