package com.portal.config;

import com.portal.model.*;
import com.portal.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository historyRepository;
    private final SavedJobRepository savedJobRepository;
    private final InterviewRepository interviewRepository;
    private final AnnouncementRepository announcementRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           StudentProfileRepository studentProfileRepository,
                           RecruiterProfileRepository recruiterProfileRepository,
                           CompanyRepository companyRepository,
                           JobRepository jobRepository,
                           ApplicationRepository applicationRepository,
                           ApplicationStatusHistoryRepository historyRepository,
                           SavedJobRepository savedJobRepository,
                           InterviewRepository interviewRepository,
                           AnnouncementRepository announcementRepository,
                           NotificationRepository notificationRepository,
                           AuditLogRepository auditLogRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.recruiterProfileRepository = recruiterProfileRepository;
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.historyRepository = historyRepository;
        this.savedJobRepository = savedJobRepository;
        this.interviewRepository = interviewRepository;
        this.announcementRepository = announcementRepository;
        this.notificationRepository = notificationRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            logger.info("Database already seeded with user accounts. Skipping initial data seed.");
            return;
        }

        logger.info("Seeding initial database records for Campus Placement Portal...");

        // 1. Create Administrator
        User admin = new User(
                "admin@campus.edu",
                passwordEncoder.encode("Admin@123"),
                "Dr. Alok Verma (TPO Admin)",
                "+91 9876543210",
                Role.ROLE_ADMIN,
                AccountStatus.ACTIVE
        );
        userRepository.save(admin);

        // 2. Create Companies
        Company google = new Company(
                "Google India Pvt Ltd",
                "Information Technology & Cloud",
                "https://careers.google.com",
                "Google LLC is an American multinational technology company focusing on artificial intelligence, search engine, cloud computing, software, and quantum computing.",
                "Bangalore / Hyderabad",
                CompanyStatus.APPROVED,
                null
        );
        companyRepository.save(google);

        Company microsoft = new Company(
                "Microsoft IDC",
                "Enterprise Software & Cloud Computing",
                "https://careers.microsoft.com",
                "Microsoft Corporation is an American multinational technology corporation producing computer software, consumer electronics, personal computers, and related services.",
                "Hyderabad / Noida / Bangalore",
                CompanyStatus.APPROVED,
                null
        );
        companyRepository.save(microsoft);

        Company tcs = new Company(
                "Tata Consultancy Services",
                "IT Services & Consulting",
                "https://www.tcs.com/careers",
                "Tata Consultancy Services is an Indian multinational information technology services and consulting company headquartered in Mumbai.",
                "Mumbai / Pune / Delhi NCR",
                CompanyStatus.APPROVED,
                null
        );
        companyRepository.save(tcs);

        Company pendingCompany = new Company(
                "CloudMatrix Technologies",
                "Cloud Infrastructure & DevOps",
                "https://cloudmatrix.example.com",
                "Innovative cloud operations startup specializing in automated Kubernetes clusters and site reliability engineering.",
                "Gurugram, Haryana",
                CompanyStatus.PENDING,
                null
        );
        companyRepository.save(pendingCompany);

        // 3. Create Recruiters
        User recruiterGoogle = new User(
                "recruiter.google@tech.com",
                passwordEncoder.encode("Recruiter@123"),
                "Sundar Raman",
                "+91 9811122233",
                Role.ROLE_RECRUITER,
                AccountStatus.ACTIVE
        );
        userRepository.save(recruiterGoogle);
        google.setCreatedByRecruiter(recruiterGoogle);
        companyRepository.save(google);

        RecruiterProfile rpGoogle = new RecruiterProfile(
                recruiterGoogle,
                google,
                "University Relations Lead",
                "Campus Hiring Team",
                "+91 9811122233"
        );
        recruiterProfileRepository.save(rpGoogle);

        User recruiterMsft = new User(
                "recruiter.msft@microsoft.com",
                passwordEncoder.encode("Recruiter@123"),
                "Kavita Deshmukh",
                "+91 9822233344",
                Role.ROLE_RECRUITER,
                AccountStatus.ACTIVE
        );
        userRepository.save(recruiterMsft);
        microsoft.setCreatedByRecruiter(recruiterMsft);
        companyRepository.save(microsoft);

        RecruiterProfile rpMsft = new RecruiterProfile(
                recruiterMsft,
                microsoft,
                "Senior Talent Acquisition Partner",
                "Azure Engineering Campus Recruitment",
                "+91 9822233344"
        );
        recruiterProfileRepository.save(rpMsft);

        User recruiterTcs = new User(
                "recruiter.tcs@tcs.com",
                passwordEncoder.encode("Recruiter@123"),
                "Ananya Sen",
                "+91 9833344455",
                Role.ROLE_RECRUITER,
                AccountStatus.ACTIVE
        );
        userRepository.save(recruiterTcs);
        tcs.setCreatedByRecruiter(recruiterTcs);
        companyRepository.save(tcs);

        RecruiterProfile rpTcs = new RecruiterProfile(
                recruiterTcs,
                tcs,
                "Campus Recruitment Manager",
                "Digital & Innovator Hiring",
                "+91 9833344455"
        );
        recruiterProfileRepository.save(rpTcs);

        // 4. Create Students
        User studentRahul = new User(
                "student.rahul@campus.edu",
                passwordEncoder.encode("Student@123"),
                "Rahul Sharma",
                "+91 9123456789",
                Role.ROLE_STUDENT,
                AccountStatus.ACTIVE
        );
        userRepository.save(studentRahul);

        StudentProfile spRahul = new StudentProfile(
                studentRahul,
                "MCA/2024/001",
                "MCA",
                "Computer Science & Applications",
                2026,
                new BigDecimal("8.85")
        );
        spRahul.setSkills("Java, Spring Boot, MySQL, REST APIs, Git, Docker, Microservices, HTML/CSS");
        spRahul.setProjects("1. Microservices E-Commerce API with Spring Boot & Docker\n2. Real-Time Chat System with WebSockets & Redis\n3. Library Automation System");
        spRahul.setCertifications("Oracle Certified Professional: Java SE 17 Developer, AWS Certified Cloud Practitioner");
        spRahul.setLinkedinUrl("https://linkedin.com/in/rahul-sharma-campus");
        spRahul.calculateProfileCompletion();
        studentProfileRepository.save(spRahul);

        User studentPriya = new User(
                "student.priya@campus.edu",
                passwordEncoder.encode("Student@123"),
                "Priya Patel",
                "+91 9234567890",
                Role.ROLE_STUDENT,
                AccountStatus.ACTIVE
        );
        userRepository.save(studentPriya);

        StudentProfile spPriya = new StudentProfile(
                studentPriya,
                "MCA/2024/002",
                "MCA",
                "Computer Science & Applications",
                2026,
                new BigDecimal("9.20")
        );
        spPriya.setSkills("Java, Python, Algorithms, Hibernate, Spring Security, React.js, PostgreSQL");
        spPriya.setProjects("1. Distributed Task Scheduler in Java\n2. AI Based Medical Diagnostics Assistant\n3. Student Attendance Face Recognition");
        spPriya.setCertifications("Google Cloud Associate Cloud Engineer, HackerRank 5-Star Problem Solving");
        spPriya.setLinkedinUrl("https://linkedin.com/in/priya-patel-campus");
        spPriya.calculateProfileCompletion();
        studentProfileRepository.save(spPriya);

        User studentAmit = new User(
                "student.amit@campus.edu",
                passwordEncoder.encode("Student@123"),
                "Amit Kumar",
                "+91 9345678901",
                Role.ROLE_STUDENT,
                AccountStatus.ACTIVE
        );
        userRepository.save(studentAmit);

        StudentProfile spAmit = new StudentProfile(
                studentAmit,
                "BT/2024/045",
                "B.Tech",
                "Computer Science Engineering",
                2026,
                new BigDecimal("7.95")
        );
        spAmit.setSkills("Java, JavaScript, Spring Boot, MySQL, Bootstrap 5, Linux");
        spAmit.setProjects("1. Campus Placement Web Portal\n2. Online Food Ordering Platform");
        spAmit.setCertifications("Java Programming Fundamentals (Coursera)");
        spAmit.setLinkedinUrl("https://linkedin.com/in/amit-kumar-campus");
        spAmit.calculateProfileCompletion();
        studentProfileRepository.save(spAmit);

        // 5. Create Job Postings
        Job jobGoogleSde = new Job(
                google,
                recruiterGoogle,
                "Software Development Engineer I (SDE-1)",
                "Join Google's core engineering team to design, build, and deploy high-performance distributed systems. Responsibilities include designing scalable REST APIs, writing clean modular Java/C++ code, and collaborating with global teams on Google Cloud infrastructure.",
                JobType.FULL_TIME,
                "Bangalore / Hyderabad",
                "INR 18,00,000 - 24,00,000 Per Annum",
                new BigDecimal("8.00"),
                "MCA, B.Tech CSE, M.Tech CSE",
                "Java, Data Structures, Algorithms, Distributed Systems, SQL, Git",
                5,
                LocalDate.now().plusDays(45)
        );
        jobRepository.save(jobGoogleSde);

        Job jobMsftCloud = new Job(
                microsoft,
                recruiterMsft,
                "Cloud Support Associate - Azure",
                "Microsoft Azure is seeking high-energy engineering graduates to architect and support enterprise cloud customers. You will diagnose complex networking, compute, and database bottlenecks across hybrid cloud environments.",
                JobType.FULL_TIME,
                "Hyderabad / Noida",
                "INR 14,00,000 - 18,00,000 Per Annum",
                new BigDecimal("7.50"),
                "MCA, B.Tech CSE, IT, Electronics",
                "Cloud Computing, Networking, Linux, Java, PowerShell/Python, Troubleshooting",
                8,
                LocalDate.now().plusDays(30)
        );
        jobRepository.save(jobMsftCloud);

        Job jobTcsIntern = new Job(
                tcs,
                recruiterTcs,
                "Full Stack Developer Intern (Digital Cadre)",
                "Fast-track internship for young developers in TCS Digital. Hands-on exposure to full-stack application lifecycle, Spring Boot microservices, modern frontends, and agile delivery.",
                JobType.INTERNSHIP,
                "Pune / Mumbai",
                "INR 35,000 / month Stipend (PPO Available: 9.0 LPA)",
                new BigDecimal("7.00"),
                "MCA, B.Tech",
                "Java, Spring Boot, JavaScript, HTML5/CSS3, SQL",
                15,
                LocalDate.now().plusDays(25)
        );
        jobRepository.save(jobTcsIntern);

        Job jobGoogleData = new Job(
                google,
                recruiterGoogle,
                "Data Engineer - Big Data Platforms",
                "Build petabyte-scale data pipelines using Apache Beam, BigQuery, and Spark. Strong database schema design and SQL optimization skills required.",
                JobType.FULL_TIME,
                "Bangalore",
                "INR 20,00,000 - 26,00,000 Per Annum",
                new BigDecimal("8.50"),
                "MCA, B.Tech CSE",
                "SQL, Python, Java, BigQuery, Hadoop, Spark",
                3,
                LocalDate.now().plusDays(60)
        );
        jobRepository.save(jobGoogleData);

        // 6. Create Applications & Status History
        // Rahul -> Google SDE (SHORTLISTED)
        Application app1 = new Application(jobGoogleSde, studentRahul, null,
                "I have strong foundations in core Java, multithreading, and algorithmic design. Having built scalable Spring microservices, I am thrilled to apply for the SDE-1 position at Google.");
        app1.setStatus(ApplicationStatus.SHORTLISTED);
        applicationRepository.save(app1);

        historyRepository.save(new ApplicationStatusHistory(app1, null, ApplicationStatus.APPLIED, "Submitted online application", studentRahul));
        historyRepository.save(new ApplicationStatusHistory(app1, ApplicationStatus.APPLIED, ApplicationStatus.SHORTLISTED, "Candidate profile shortlisted based on CGPA and technical projects", recruiterGoogle));

        // Rahul -> Microsoft Cloud (INTERVIEW_SCHEDULED)
        Application app2 = new Application(jobMsftCloud, studentRahul, null,
                "With my AWS Cloud Practitioner certification and solid networking foundations, I am eager to contribute to Microsoft Azure's cloud support team.");
        app2.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
        applicationRepository.save(app2);

        historyRepository.save(new ApplicationStatusHistory(app2, null, ApplicationStatus.APPLIED, "Submitted online application", studentRahul));
        historyRepository.save(new ApplicationStatusHistory(app2, ApplicationStatus.APPLIED, ApplicationStatus.SHORTLISTED, "Resume shortlisted by technical panel", recruiterMsft));
        historyRepository.save(new ApplicationStatusHistory(app2, ApplicationStatus.SHORTLISTED, ApplicationStatus.INTERVIEW_SCHEDULED, "Round 1 Technical Interview scheduled", recruiterMsft));

        // Priya -> Google SDE (SELECTED)
        Application app3 = new Application(jobGoogleSde, studentPriya, null,
                "As an MCA student with a 9.20 CGPA and 5-star problem-solving recognition, I am passionate about tackling Google's algorithmic scale.");
        app3.setStatus(ApplicationStatus.SELECTED);
        applicationRepository.save(app3);

        historyRepository.save(new ApplicationStatusHistory(app3, null, ApplicationStatus.APPLIED, "Application submitted", studentPriya));
        historyRepository.save(new ApplicationStatusHistory(app3, ApplicationStatus.APPLIED, ApplicationStatus.SHORTLISTED, "Shortlisted", recruiterGoogle));
        historyRepository.save(new ApplicationStatusHistory(app3, ApplicationStatus.SHORTLISTED, ApplicationStatus.INTERVIEW_SCHEDULED, "Interview rounds cleared", recruiterGoogle));
        historyRepository.save(new ApplicationStatusHistory(app3, ApplicationStatus.INTERVIEW_SCHEDULED, ApplicationStatus.SELECTED, "Candidate offered SDE-1 position with CTC 22 LPA", recruiterGoogle));

        // Amit -> TCS Intern (APPLIED)
        Application app4 = new Application(jobTcsIntern, studentAmit, null,
                "Excited to begin my professional software development career with TCS Digital internship program.");
        app4.setStatus(ApplicationStatus.APPLIED);
        applicationRepository.save(app4);
        historyRepository.save(new ApplicationStatusHistory(app4, null, ApplicationStatus.APPLIED, "Application submitted", studentAmit));

        // 7. Schedule an Interview for Rahul (Microsoft Cloud)
        Interview interview1 = new Interview(
                app2,
                InterviewMode.ONLINE,
                LocalDate.now().plusDays(2),
                LocalTime.of(11, 0),
                "https://teams.microsoft.com/l/meetup-join/campus-azure-interview",
                "Senior Cloud Architect - Azure Engineering",
                "Please have a stable internet connection, code editor ready, and be prepared to discuss networking, DNS, and multithreading concepts."
        );
        interviewRepository.save(interview1);

        // 8. Saved Jobs
        savedJobRepository.save(new SavedJob(studentRahul, jobGoogleData));
        savedJobRepository.save(new SavedJob(studentPriya, jobMsftCloud));

        // 9. University Announcements
        Announcement ann1 = new Announcement(
                admin,
                "Campus Recruitment Drive 2026 Commences: Google, Microsoft & TCS On-Campus",
                "We are thrilled to welcome our premier recruiting partners for the 2026 Batch placement season. All eligible students are advised to review eligibility criteria, keep their resumes updated on the portal, and check their application dashboards daily for interview alerts.",
                "ALL",
                "HIGH"
        );
        announcementRepository.save(ann1);

        Announcement ann2 = new Announcement(
                admin,
                "Mandatory Resume & Academic Profile Verification for MCA Final Year",
                "All final-year MCA students must complete their profile details, upload their latest PDF resume, and ensure their CGPA and enrollment numbers match official university marksheets prior to Friday 5:00 PM.",
                "STUDENTS",
                "NORMAL"
        );
        announcementRepository.save(ann2);

        // 10. Initial Notifications
        notificationRepository.save(new Notification(
                studentRahul,
                recruiterMsft,
                "Interview Scheduled: Cloud Support Associate",
                "Your technical interview with Microsoft IDC has been scheduled for " + LocalDate.now().plusDays(2) + " at 11:00 AM via Microsoft Teams.",
                "/student/interviews",
                NotificationType.INTERVIEW_SCHEDULED
        ));

        notificationRepository.save(new Notification(
                studentPriya,
                recruiterGoogle,
                "Congratulations! Job Offer Released",
                "You have been selected for the position of Software Development Engineer I at Google India Pvt Ltd!",
                "/student/applications",
                NotificationType.APPLICATION_UPDATE
        ));

        // 11. Initial Audit Logs
        auditLogRepository.save(new AuditLog(admin, "SYSTEM_INIT", "System", 1L, "Initial seed data loaded successfully into campus placement portal", "127.0.0.1"));
        auditLogRepository.save(new AuditLog(recruiterGoogle, "POST_JOB", "Job", jobGoogleSde.getId(), "Recruiter posted SDE-1 opportunity", "127.0.0.1"));

        logger.info("Database successfully initialized with initial demonstration data.");
    }
}
