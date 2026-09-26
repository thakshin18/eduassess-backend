package com.eduassess.config;

import com.eduassess.entity.*;
import com.eduassess.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Profile;
import java.util.Arrays;
import java.util.Optional;
import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    @Profile("!test")
    public CommandLineRunner seedDatabase(UserRepository userRepository,
                                          TestRepository testRepository,
                                          QuestionRepository questionRepository,
                                          ResultRepository resultRepository,
                                          PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. Seed Users
            createUser(userRepository, passwordEncoder, "Admin User", "admin@example.com", "admin123", Role.ADMIN, null);
            createUser(userRepository, passwordEncoder, "Alice Smith", "alice@example.com", "student123", Role.STUDENT, "MCA-1");
            createUser(userRepository, passwordEncoder, "Bob Johnson", "bob@example.com", "student123", Role.STUDENT, "MCA-2");
            createUser(userRepository, passwordEncoder, "Charlie Brown", "charlie@example.com", "student123", Role.STUDENT, "MCA-3");
            System.out.println("Seeded initial users.");

            // 2. Cleanup old generic tests if they exist
            List<String> oldTestTitles = Arrays.asList("Core Physics", "Advanced Mathematics", "World History Basics", "Chemistry Lab Safety");
            for (String oldTitle : oldTestTitles) {
                testRepository.findByTitle(oldTitle).ifPresent(testRepository::delete);
            }

            // 3. Seed MCA Tests
            seedTest1(testRepository, questionRepository, resultRepository);
            seedTest2(testRepository, questionRepository, resultRepository);
            seedTest3(testRepository, questionRepository, resultRepository);
            seedTest4(testRepository, questionRepository, resultRepository);
            seedTest5(testRepository, questionRepository, resultRepository);
            System.out.println("Seeded MCA assessment tests.");
        };
    }

    private void seedTest1(TestRepository testRepo, QuestionRepository qRepo, ResultRepository rRepo) {
        String title = "MCA Programming & Data Structures";
        com.eduassess.entity.Test t = handleExistingTest(testRepo, qRepo, rRepo, title, "Core concepts of programming, OOP, and data structures.", "Programming", 60, 20, 10);
        if (t == null) return; // Already exists with sufficient questions

        createQuestionWithOptions(qRepo, t, 1, 2, "What is the time complexity of binary search?", "O(n)", false, "O(n log n)", false, "O(log n)", true, "O(1)", false);
        createQuestionWithOptions(qRepo, t, 2, 2, "Which data structure uses LIFO (Last In First Out)?", "Queue", false, "Stack", true, "Linked List", false, "Tree", false);
        createQuestionWithOptions(qRepo, t, 3, 2, "What is a pure virtual function in C++?", "A function with no body", false, "A virtual function equated to zero", true, "A function that returns void", false, "An inline function", false);
        createQuestionWithOptions(qRepo, t, 4, 2, "Which algorithm is used to find the shortest path in a graph?", "Depth First Search", false, "Kruskal's Algorithm", false, "Dijkstra's Algorithm", true, "Merge Sort", false);
        createQuestionWithOptions(qRepo, t, 5, 2, "What is the primary benefit of encapsulation in OOP?", "Code reusability", false, "Data hiding and security", true, "Faster execution", false, "Multiple inheritance", false);
        createQuestionWithOptions(qRepo, t, 6, 2, "Which sorting algorithm has the worst-case time complexity of O(n^2)?", "Merge Sort", false, "Heap Sort", false, "Quick Sort", true, "Radix Sort", false);
        createQuestionWithOptions(qRepo, t, 7, 2, "What is a memory leak?", "RAM being physically damaged", false, "Failure to release allocated memory", true, "A virus that deletes files", false, "Disk fragmentation", false);
        createQuestionWithOptions(qRepo, t, 8, 2, "Which collection in Java implements a doubly-linked list?", "ArrayList", false, "Vector", false, "LinkedList", true, "PriorityQueue", false);
        createQuestionWithOptions(qRepo, t, 9, 2, "What is an abstract class?", "A class with only static methods", false, "A class that cannot be instantiated", true, "A class with no methods", false, "A class that cannot be inherited", false);
        createQuestionWithOptions(qRepo, t, 10, 2, "In a binary search tree, where is the smallest element located?", "Root", false, "Rightmost node", false, "Leftmost node", true, "Any leaf node", false);
    }

    private void seedTest2(TestRepository testRepo, QuestionRepository qRepo, ResultRepository rRepo) {
        String title = "MCA Database Management Systems";
        com.eduassess.entity.Test t = handleExistingTest(testRepo, qRepo, rRepo, title, "Database design, SQL, normalization, and transactions.", "DBMS", 60, 20, 10);
        if (t == null) return;

        createQuestionWithOptions(qRepo, t, 1, 2, "What does ACID stand for in DBMS?", "Atomicity, Consistency, Isolation, Durability", true, "Accuracy, Consistency, Integration, Durability", false, "Atomicity, Concurrency, Isolation, Durability", false, "Accuracy, Concurrency, Isolation, Dependency", false);
        createQuestionWithOptions(qRepo, t, 2, 2, "Which normal form removes transitive dependencies?", "1NF", false, "2NF", false, "3NF", true, "BCNF", false);
        createQuestionWithOptions(qRepo, t, 3, 2, "What is the primary purpose of an index in a database?", "To enforce foreign keys", false, "To speed up data retrieval", true, "To encrypt data", false, "To reduce database size", false);
        createQuestionWithOptions(qRepo, t, 4, 2, "Which SQL command is used to add a new column to a table?", "INSERT COLUMN", false, "ADD COLUMN", false, "ALTER TABLE", true, "MODIFY TABLE", false);
        createQuestionWithOptions(qRepo, t, 5, 2, "What is a foreign key?", "A key used to encrypt the database", false, "A key that uniquely identifies a row", false, "A key that references a primary key in another table", true, "A key used for indexing", false);
        createQuestionWithOptions(qRepo, t, 6, 2, "Which JOIN returns all records when there is a match in either left or right table?", "INNER JOIN", false, "LEFT JOIN", false, "RIGHT JOIN", false, "FULL OUTER JOIN", true);
        createQuestionWithOptions(qRepo, t, 7, 2, "What is a dirty read?", "Reading uncommitted data from another transaction", true, "Reading committed data from another transaction", false, "Reading data that has been deleted", false, "Reading encrypted data", false);
        createQuestionWithOptions(qRepo, t, 8, 2, "Which SQL keyword is used to sort the result-set?", "ORDER BY", true, "SORT BY", false, "GROUP BY", false, "ALIGN BY", false);
        createQuestionWithOptions(qRepo, t, 9, 2, "What is the difference between TRUNCATE and DELETE?", "DELETE cannot be rolled back, TRUNCATE can", false, "TRUNCATE removes the table structure", false, "TRUNCATE is faster and cannot be rolled back", true, "There is no difference", false);
        createQuestionWithOptions(qRepo, t, 10, 2, "In an ER diagram, what does an ellipse represent?", "Entity", false, "Relationship", false, "Attribute", true, "Primary Key", false);
    }

    private void seedTest3(TestRepository testRepo, QuestionRepository qRepo, ResultRepository rRepo) {
        String title = "MCA Computer Networks & Cyber Security";
        com.eduassess.entity.Test t = handleExistingTest(testRepo, qRepo, rRepo, title, "Networking models, protocols, and fundamental security.", "Networking", 60, 20, 10);
        if (t == null) return;

        createQuestionWithOptions(qRepo, t, 1, 2, "How many layers are in the OSI model?", "5", false, "7", true, "4", false, "6", false);
        createQuestionWithOptions(qRepo, t, 2, 2, "Which protocol is responsible for resolving domain names to IP addresses?", "HTTP", false, "FTP", false, "DNS", true, "DHCP", false);
        createQuestionWithOptions(qRepo, t, 3, 2, "What is the primary difference between TCP and UDP?", "TCP is connectionless, UDP is connection-oriented", false, "TCP is faster than UDP", false, "TCP guarantees delivery, UDP does not", true, "TCP is used for video streaming, UDP for emails", false);
        createQuestionWithOptions(qRepo, t, 4, 2, "Which device operates at the Network Layer (Layer 3)?", "Switch", false, "Router", true, "Hub", false, "Repeater", false);
        createQuestionWithOptions(qRepo, t, 5, 2, "What is the standard port for HTTPS?", "80", false, "443", true, "21", false, "22", false);
        createQuestionWithOptions(qRepo, t, 6, 2, "What is a MAC address?", "A logical network address", false, "A hardware address assigned to a network interface", true, "A routing protocol", false, "A type of firewall", false);
        createQuestionWithOptions(qRepo, t, 7, 2, "Which attack involves flooding a target with traffic to make it unavailable?", "Phishing", false, "Man-in-the-Middle", false, "SQL Injection", false, "DDoS", true);
        createQuestionWithOptions(qRepo, t, 8, 2, "What does a firewall do?", "Encrypts all network traffic", false, "Filters incoming and outgoing network traffic", true, "Boosts Wi-Fi signal", false, "Resolves IP addresses", false);
        createQuestionWithOptions(qRepo, t, 9, 2, "In cryptography, what is a public key used for?", "Decryption only", false, "Encryption and verifying signatures", true, "Creating digital certificates", false, "Hashing data", false);
        createQuestionWithOptions(qRepo, t, 10, 2, "Which IPv4 address class is primarily used for multicasting?", "Class A", false, "Class B", false, "Class C", false, "Class D", true);
    }

    private void seedTest4(TestRepository testRepo, QuestionRepository qRepo, ResultRepository rRepo) {
        String title = "MCA Operating Systems";
        com.eduassess.entity.Test t = handleExistingTest(testRepo, qRepo, rRepo, title, "Process management, memory management, and file systems.", "OS", 60, 20, 10);
        if (t == null) return;

        createQuestionWithOptions(qRepo, t, 1, 2, "What is a process in an operating system?", "A program in execution", true, "A file on the hard drive", false, "A hardware component", false, "A network connection", false);
        createQuestionWithOptions(qRepo, t, 2, 2, "Which CPU scheduling algorithm gives minimum average waiting time?", "FCFS", false, "SJF", true, "Round Robin", false, "Priority Scheduling", false);
        createQuestionWithOptions(qRepo, t, 3, 2, "What is virtual memory?", "Memory used by virtual machines", false, "RAM that is installed on the motherboard", false, "A technique that uses disk space as an extension of RAM", true, "Cache memory inside the CPU", false);
        createQuestionWithOptions(qRepo, t, 4, 2, "What is a deadlock?", "When a process finishes execution", false, "When two or more processes are waiting indefinitely for each other", true, "When the OS crashes", false, "When memory is full", false);
        createQuestionWithOptions(qRepo, t, 5, 2, "Which synchronization tool is used to solve the critical section problem?", "Compiler", false, "Semaphore", true, "Linker", false, "Loader", false);
        createQuestionWithOptions(qRepo, t, 6, 2, "What does PCB stand for in an OS?", "Printed Circuit Board", false, "Process Control Block", true, "Program Counter Base", false, "Primary Cache Buffer", false);
        createQuestionWithOptions(qRepo, t, 7, 2, "What is thrashing?", "High disk activity due to excessive page faults", true, "A virus deleting system files", false, "CPU executing too many instructions", false, "A deadlock prevention technique", false);
        createQuestionWithOptions(qRepo, t, 8, 2, "Which command is used in Linux to list files?", "dir", false, "list", false, "ls", true, "show", false);
        createQuestionWithOptions(qRepo, t, 9, 2, "What is the purpose of a system call?", "To communicate with other computers", false, "To request a service from the operating system kernel", true, "To call a user-defined function", false, "To restart the computer", false);
        createQuestionWithOptions(qRepo, t, 10, 2, "Which memory allocation strategy allocates the smallest hole that is big enough?", "First Fit", false, "Worst Fit", false, "Next Fit", false, "Best Fit", true);
    }

    private void seedTest5(TestRepository testRepo, QuestionRepository qRepo, ResultRepository rRepo) {
        String title = "MCA Software Engineering & Web Technologies";
        com.eduassess.entity.Test t = handleExistingTest(testRepo, qRepo, rRepo, title, "SDLC, Agile, UML, and modern web APIs.", "Software Engineering", 60, 20, 10);
        if (t == null) return;

        createQuestionWithOptions(qRepo, t, 1, 2, "What does SDLC stand for?", "Software Design Life Cycle", false, "Software Development Life Cycle", true, "System Development Logic Cycle", false, "Standard Design Language Code", false);
        createQuestionWithOptions(qRepo, t, 2, 2, "Which Agile framework uses short, time-boxed iterations called Sprints?", "Waterfall", false, "Kanban", false, "Scrum", true, "Spiral", false);
        createQuestionWithOptions(qRepo, t, 3, 2, "What is the purpose of UML?", "To write executable code", false, "To visualize the design of a system", true, "To manage a database", false, "To format web pages", false);
        createQuestionWithOptions(qRepo, t, 4, 2, "In REST APIs, which HTTP method is typically used to update an existing resource?", "GET", false, "POST", false, "PUT", true, "DELETE", false);
        createQuestionWithOptions(qRepo, t, 5, 2, "What does CSS stand for?", "Cascading Style Sheets", true, "Computer Style Sheets", false, "Creative Style System", false, "Colorful Style Sheets", false);
        createQuestionWithOptions(qRepo, t, 6, 2, "What is unit testing?", "Testing the entire system as a whole", false, "Testing individual components or functions of a software", true, "Testing the software with real users", false, "Testing the software's performance under load", false);
        createQuestionWithOptions(qRepo, t, 7, 2, "Which version control system is distributed?", "SVN", false, "CVS", false, "Git", true, "TFS", false);
        createQuestionWithOptions(qRepo, t, 8, 2, "What is the DOM in web development?", "Data Object Model", false, "Document Object Model", true, "Dynamic Output Method", false, "Design Oriented Model", false);
        createQuestionWithOptions(qRepo, t, 9, 2, "Which architectural pattern separates data, user interface, and control logic?", "Client-Server", false, "Microservices", false, "MVC (Model-View-Controller)", true, "Peer-to-Peer", false);
        createQuestionWithOptions(qRepo, t, 10, 2, "What is a primary characteristic of a Microservices architecture?", "A single, monolithic codebase", false, "Tightly coupled components", false, "Small, independent services communicating over a network", true, "Shared database for all services", false);
    }

    private com.eduassess.entity.Test handleExistingTest(TestRepository testRepo, QuestionRepository qRepo, ResultRepository rRepo, String title, String description, String subject, int duration, int totalMarks, int passingMarks) {
        Optional<com.eduassess.entity.Test> existingOpt = testRepo.findByTitle(title);
        com.eduassess.entity.Test t;
        
        if (existingOpt.isPresent()) {
            t = existingOpt.get();
            if (qRepo.countByTestId(t.getId()) >= 10) {
                // Test exists and has sufficient questions, skip reseeding
                return null;
            } else {
                // Not enough questions. Repair by deleting existing answers/results, then questions, then test, and re-create.
                List<Result> results = rRepo.findByTestId(t.getId());
                rRepo.deleteAll(results);
                testRepo.delete(t);
            }
        }
        
        t = com.eduassess.entity.Test.builder()
                .title(title)
                .description(description)
                .subject(subject)
                .difficulty("Medium")
                .duration(duration)
                .totalMarks(totalMarks)
                .passingMarks(passingMarks)
                .status(TestStatus.PUBLISHED)
                .build();
        return testRepo.save(t);
    }

    private void createUser(UserRepository repo, PasswordEncoder encoder, String name, String email, String password, Role role, String grade) {
        Optional<User> existing = repo.findByEmail(email);
        if (existing.isEmpty()) {
            User u = new User();
            u.setName(name);
            u.setEmail(email);
            u.setPassword(encoder.encode(password));
            u.setRole(role);
            u.setGrade(grade);
            repo.save(u);
        }
    }

    private void createQuestionWithOptions(QuestionRepository qRepo, com.eduassess.entity.Test test, int order, int marks, String text,
                                           String o1Text, boolean o1Corr,
                                           String o2Text, boolean o2Corr,
                                           String o3Text, boolean o3Corr,
                                           String o4Text, boolean o4Corr) {
        Question q = new Question();
        q.setTest(test);
        q.setQuestionOrder(order);
        q.setMarks(marks);
        q.setQuestionText(text);

        Option o1 = new Option(); o1.setQuestion(q); o1.setOptionOrder(1); o1.setOptionText(o1Text); o1.setIsCorrect(o1Corr);
        Option o2 = new Option(); o2.setQuestion(q); o2.setOptionOrder(2); o2.setOptionText(o2Text); o2.setIsCorrect(o2Corr);
        Option o3 = new Option(); o3.setQuestion(q); o3.setOptionOrder(3); o3.setOptionText(o3Text); o3.setIsCorrect(o3Corr);
        Option o4 = new Option(); o4.setQuestion(q); o4.setOptionOrder(4); o4.setOptionText(o4Text); o4.setIsCorrect(o4Corr);

        q.setOptions(Arrays.asList(o1, o2, o3, o4));
        qRepo.save(q);
    }
}
