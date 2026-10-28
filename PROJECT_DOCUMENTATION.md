# Campus Placement and Internship Portal
## MCA Master's Minor Project Technical Documentation & Architecture Report

---

### Project Metadata
- **Project Title:** Campus Placement and Internship Portal
- **Academic Degree:** Master of Computer Applications (MCA) Minor Project
- **Technology Stack:** Java 26 / 21, Spring Boot 3.3.5, Spring Security 6, Spring Data JPA, Spring JDBC (JdbcTemplate), Hibernate 6, MySQL 8+, Thymeleaf, Bootstrap 5, Maven
- **Architecture Pattern:** Multi-Tier MVC / Layered Enterprise Domain Architecture

---

## 1. Project Abstract
The **Campus Placement and Internship Portal** is a centralized, role-based enterprise web application architected to digitize and streamline the university campus recruitment lifecycle. Traditional academic placement systems rely heavily on scattered spreadsheets, manual email chains, and bulletin board notifications, resulting in data redundancy, delayed candidate alerts, unauthorized data access, and a lack of real-time placement analytics.

This system provides a unified, secure platform serving three key university stakeholders: **Students**, **Corporate Recruiters**, and **Training and Placement Officers (Administrators)**. Built using Spring Boot, Spring Security with BCrypt hashing, Spring Data JPA with MySQL, and high-performance aggregate reporting with Spring `JdbcTemplate`, the portal facilitates profile maintenance, verified resume upload, corporate accreditation, job postings with eligibility filtering, one-click applications with duplicate prevention, real-time interview scheduling, multi-channel notifications, and comprehensive placement intelligence.

---

## 2. Problem Statement
In higher education institutions, placement offices face distinct operational bottlenecks:
1. **Disparate Data Stores:** Student academic records (CGPAs, backlogs, branch codes) and resumes are maintained in disconnected spreadsheets, making eligibility cross-checks prone to human error.
2. **Lack of Real-Time Status Tracking:** Students lack visibility into their application lifecycle (shortlisting, interview rounds, offers).
3. **Unregulated Employer Registrations:** Fake or unverified companies could attempt to contact students without institutional accreditation.
4. **Security & Ownership Vulnerabilities:** Conventional prototypes fail to enforce server-side ownership checks, allowing users to modify URLs or parameters to access unauthorized profiles or resumes.
5. **Slow Analytical Reporting:** Generating department-wise, company-wise, and gender-wise placement percentages for accreditation bodies (NAAC/NBA) requires days of manual data collation.

---

## 3. Project Objectives
- **Centralize Recruitment Operations:** Provide a single portal for job postings, student applications, interview scheduling, and offer letters.
- **Enforce Role-Based Access Control (RBAC):** Restrict access strictly across Student, Recruiter, and Administrator endpoints.
- **Automate Eligibility Verification:** Validate student eligibility (minimum CGPA and permitted academic branches) before allowing application submission.
- **Ensure Data Integrity & Duplicate Prevention:** Restrict students to a single active application per job opening and log all status transitions in an audit trail.
- **Deliver Fast Placement Analytics:** Use Spring JDBC `JdbcTemplate` for high-throughput relational SQL aggregation.
- **Provide Career Preparation Resources:** Deliver curated interview preparation modules (Aptitude, Java, DBMS, DSA, HR, and an 8-week preparation roadmap) inline.

---

## 4. Scope
### In-Scope
- User authentication and role-based authorization (Student, Recruiter, Admin).
- Secure password reset workflow with cryptographically random tokens and expiry.
- Student profile management (enrollment, course, branch, CGPA, skills, projects, certifications, LinkedIn).
- PDF resume upload with MIME-type verification, UUID file renaming, and path-traversal prevention.
- Employer registration and mandatory administrative accreditation.
- Job and internship posting with deadline and vacancy management.
- Application tracking with multi-stage status lifecycle and audit history.
- Bookmarking / saved jobs.
- Virtual and offline interview scheduling with candidate notifications.
- Admin dashboard with placement intelligence and system diagnostics.
- Career preparation knowledge base.

### Out-of-Scope (Future Enhancements)
- Automated resume parsing via AI/OCR.
- In-app WebRTC video conferencing for interviews.
- SMS gateway integration for offline alerts.

---

## 5. Existing System vs. Proposed System

| Feature / Dimension | Existing Manual System | Proposed Campus Placement Portal |
| :--- | :--- | :--- |
| **Data Storage** | Excel sheets, Google Forms, paper forms | Normalized MySQL relational database |
| **Eligibility Verification** | Manual manual spreadsheet filtering | Server-side automated CGPA & branch validation |
| **Duplicate Prevention** | Difficult to catch; requires manual deduplication | Enforced via unique composite constraints & JPA checks |
| **Company Verification** | Unregulated email communication | Mandatory administrator accreditation workflow |
| **Application Tracking** | Opaque; students wait for manual email blasts | Real-time status badges with audit timestamp history |
| **Interview Scheduling** | Ad-hoc calendar invites | Integrated interview scheduler with meeting links & alerts |
| **Security & Privacy** | Resumes shared openly on cloud drives | Role-based authorized download endpoints |
| **Reports & Analytics** | Days of manual aggregation | Instant SQL-aggregated reports via JdbcTemplate |

---

## 6. System Architecture

The application adopts a **Layered Multi-Tier Enterprise Architecture**:

```
+---------------------------------------------------------------------------------+
|                               PRESENTATION TIER                                |
|   Thymeleaf 3 Templates  |  Bootstrap 5.3  |  Bootstrap Icons  |  Vanilla JS    |
+---------------------------------------------------------------------------------+
                                       |
                                       v
+---------------------------------------------------------------------------------+
|                                CONTROLLER TIER                                 |
|  AuthController | HomeController | StudentController | RecruiterController      |
|  AdminController | CareerPrepController | GlobalExceptionHandler                |
+---------------------------------------------------------------------------------+
                                       |
                                       v
+---------------------------------------------------------------------------------+
|                             SECURITY & SERVICE TIER                             |
|  Spring Security 6 (DaoAuthenticationProvider, BCrypt, Role-Based Matchers)     |
|  UserService | StudentService | RecruiterService | CompanyService               |
|  JobService  | ApplicationService | InterviewService | NotificationService      |
|  ResumeStorageService | ReportService | DiagnosticService | AuditLogService     |
+---------------------------------------------------------------------------------+
                    |                                          |
                    v (ORM / Entities)                         v (Direct Aggregations)
+----------------------------------------+   +------------------------------------+
|            SPRING DATA JPA             |   |         SPRING JDBCTEMPLATE        |
|    Repositories & Hibernate 6          |   |  Direct SQL Relational Queries     |
+----------------------------------------+   +------------------------------------+
                    \                                          /
                     v                                        v
+---------------------------------------------------------------------------------+
|                                 DATABASE TIER                                   |
|            MySQL 8+ Relational Engine (campus_placement_portal_one)             |
|   users | student_profiles | recruiter_profiles | companies | jobs              |
|   applications | application_status_history | interviews | resumes              |
|   saved_jobs | notifications | announcements | audit_logs | reset_tokens        |
+---------------------------------------------------------------------------------+
```

---

## 7. Functional Requirements
### 7.1 Student Module
- Register with university enrollment number, program, branch, and CGPA.
- Authenticate securely and manage password reset.
- Upload/replace resume file (PDF/DOC, max 5MB).
- Browse active, non-expired campus vacancies.
- Search and filter opportunities by keyword, employment type, location.
- Verify eligibility in real time and submit application with optional cover note.
- Prevent duplicate applications to the same job.
- Save/unsave opportunities to personal bookmarks.
- Track application stages (`APPLIED`, `REVIEWING`, `SHORTLISTED`, `INTERVIEW_SCHEDULED`, `SELECTED`, `REJECTED`, `WITHDRAWN`).
- View scheduled interview timings, venue, or video conference links.
- Read and clear notifications.
- Study career preparation resources.

### 7.2 Recruiter Module
- Register recruiter account and associate with a company.
- Submit company accreditation details (industry, website, description, location) for admin review.
- Create, edit, publish, and close job postings.
- Specify minimum CGPA, eligible branches, openings, CTC/stipend, and deadline.
- View applicants for company postings with academic summaries.
- Download candidate resumes securely.
- Shortlist, review, or reject applicants with evaluation feedback.
- Schedule interview rounds (mode, date, time, interviewer, instructions).
- View candidate interview schedules.

### 7.3 Administrator Module
- Access master dashboard with real-time database counts.
- Manage user account statuses (`ACTIVE`, `INACTIVE`, `SUSPENDED`).
- Review and approve/reject company accreditation requests.
- Moderate campus job postings across all recruiters.
- Broadcast university announcements targeted to all, students, or recruiters.
- View real-time placement reports (by company and by branch).
- Inspect immutable audit trail of user activities and IP addresses.
- Monitor JVM runtime diagnostics and MySQL table row statistics.

---

## 8. Non-Functional Requirements
- **Security:** Passwords encrypted using BCrypt (cost factor 10); CSRF tokens enabled on all mutating forms; role authorization enforced on URL patterns; path-traversal prevention on file storage.
- **Performance:** Dynamic indexed lookups; aggregate analytics executed via `JdbcTemplate` to bypass ORM entity overhead; zero eager loading on large collections.
- **Reliability & Consistency:** ACID compliance guaranteed across multi-table operations (`@Transactional`).
- **Usability:** Responsive Bootstrap 5 user interface compatible across mobile, tablet, and desktop screens.
- **Maintainability:** Separation of concerns following standard Spring layered architecture.

---

## 9. Entity-Relationship (ER) Diagram

```
+------------------+         1:1         +----------------------+
|      users       | ------------------- |   student_profiles   |
|------------------|                     |----------------------|
| PK id            |                     | PK id                |
|    email (UQ)    |                     | FK user_id (UQ)      |
|    password      |                     |    enrollment_no (UQ)|
|    full_name     |                     |    course, branch    |
|    phone         |                     |    cgpa, graduation  |
|    role          |                     |    skills, projects  |
|    status        |                     +----------------------+
+------------------+                                |
       |       | 1:1                                | 1:N
       |       +-------------+                      |
       |                     |                      |
       | 1:N                 v                      v
       |           +----------------------+  +------------------+
       |           |  recruiter_profiles  |  |     resumes      |
       |           |----------------------|  |------------------|
       |           | PK id                |  | PK id            |
       |           | FK user_id (UQ)      |  | FK student_id    |
       |           | FK company_id        |  |    file_name     |
       |           |    designation       |  |    stored_path   |
       |           +----------------------+  +------------------+
       |                                            |
       v 1:N                                        v 1:N
+------------------+      1:N     +-------------------------------+
|    companies     | -----------> |             jobs              |
|------------------|              |-------------------------------|
| PK id            |              | PK id                         |
|    name (UQ)     |              | FK company_id                 |
|    industry      |              | FK recruiter_id               |
|    approval_stat |              |    title, description         |
| FK recruiter_id  |              |    min_cgpa, allowed_branches |
+------------------+              |    salary, deadline, status   |
                                  +-------------------------------+
                                                  |
                                                  | 1:N
                                                  v
                                      +-----------------------+
                                      |     applications      |
                                      |-----------------------|
                                      | PK id                 |
                                      | FK job_id             |
                                      | FK student_id         |
                                      | FK resume_id          |
                                      |    status             |
                                      |    cover_letter       |
                                      +-----------------------+
                                           |             |
                         +-----------------+             +-----------------+
                         | 1:N                                         1:N |
                         v                                                 v
         +--------------------------------+               +--------------------------------+
         |   application_status_history   |               |           interviews           |
         |--------------------------------|               |--------------------------------|
         | PK id                          |               | PK id                          |
         | FK application_id              |               | FK application_id              |
         |    previous_status             |               |    mode (ONLINE/OFFLINE)       |
         |    new_status                  |               |    interview_date              |
         |    remarks                     |               |    interview_time              |
         | FK changed_by_id               |               |    location_or_link            |
         +--------------------------------+               |    interviewer_name, status    |
                                                          +--------------------------------+
```

---

## 10. Database Schema Documentation

### 1. `users`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique user identifier |
| `email` | VARCHAR(150) | NOT NULL, UNIQUE, INDEX | User login email |
| `password` | VARCHAR(255) | NOT NULL | BCrypt password hash |
| `full_name` | VARCHAR(100) | NOT NULL | User's full name |
| `phone` | VARCHAR(20) | NULLABLE | Primary mobile number |
| `role` | VARCHAR(30) | NOT NULL | `ROLE_STUDENT`, `ROLE_RECRUITER`, `ROLE_ADMIN` |
| `status` | VARCHAR(30) | NOT NULL, DEFAULT 'ACTIVE' | `ACTIVE`, `INACTIVE`, `SUSPENDED` |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Registration date |
| `updated_at` | TIMESTAMP | AUTO UPDATE | Last update timestamp |

### 2. `student_profiles`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Profile identifier |
| `user_id` | BIGINT | NOT NULL, UNIQUE, FK -> users(id) | Associated user account |
| `enrollment_number` | VARCHAR(50) | NOT NULL, UNIQUE, INDEX | University roll/enrollment number |
| `course` | VARCHAR(100) | NOT NULL | Degree program (e.g. MCA, B.Tech) |
| `branch` | VARCHAR(100) | NOT NULL | Specialization / Academic department |
| `graduation_year` | INT | NOT NULL | Passing out batch year |
| `cgpa` | DECIMAL(4,2) | NOT NULL | Cumulative Grade Point Average |
| `skills` | TEXT | NULLABLE | Comma-separated technical skills |
| `projects` | TEXT | NULLABLE | Academic and personal projects summary |
| `certifications` | TEXT | NULLABLE | Professional certifications |
| `linkedin_url` | VARCHAR(255) | NULLABLE | LinkedIn public profile link |
| `profile_completion`| INT | DEFAULT 50 | Calculated profile strength % (0-100) |

### 3. `companies`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Company record ID |
| `name` | VARCHAR(150) | NOT NULL, UNIQUE | Registered company name |
| `industry` | VARCHAR(100) | NULLABLE | Industry sector |
| `website` | VARCHAR(255) | NULLABLE | Corporate URL |
| `description` | TEXT | NULLABLE | Overview of organization |
| `location` | VARCHAR(150) | NULLABLE | Primary office/HQ location |
| `approval_status` | VARCHAR(30) | NOT NULL, DEFAULT 'PENDING' | `PENDING`, `APPROVED`, `REJECTED` |
| `created_by_recruiter_id` | BIGINT | FK -> users(id) | Recruiter who registered company |

### 4. `jobs`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Job posting ID |
| `company_id` | BIGINT | NOT NULL, FK -> companies(id) | Hiring employer |
| `recruiter_id` | BIGINT | NOT NULL, FK -> users(id) | Recruiter who posted the role |
| `title` | VARCHAR(150) | NOT NULL | Job title |
| `description` | MEDIUMTEXT | NOT NULL | Comprehensive job role & responsibilities |
| `job_type` | VARCHAR(50) | NOT NULL | `FULL_TIME`, `INTERNSHIP`, `PART_TIME`, `CONTRACT` |
| `location` | VARCHAR(150) | NOT NULL | Job work location / Remote |
| `salary_or_stipend`| VARCHAR(100) | NOT NULL | Compensation package / CTC |
| `min_cgpa` | DECIMAL(4,2) | DEFAULT 0.00 | Minimum eligibility CGPA cutoff |
| `allowed_branches` | VARCHAR(255) | NULLABLE | Comma-separated eligible branches |
| `required_skills` | TEXT | NULLABLE | Technical competencies |
| `openings` | INT | DEFAULT 1 | Total vacancies |
| `deadline` | DATE | NOT NULL | Application close date |
| `status` | VARCHAR(30) | NOT NULL, DEFAULT 'ACTIVE' | `ACTIVE`, `CLOSED` |

### 5. `applications`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Application record ID |
| `job_id` | BIGINT | NOT NULL, FK -> jobs(id) | Applied job opening |
| `student_id` | BIGINT | NOT NULL, FK -> users(id) | Applicant student |
| `resume_id` | BIGINT | NULLABLE, FK -> resumes(id) | Attached resume file |
| `status` | VARCHAR(40) | NOT NULL, DEFAULT 'APPLIED' | `APPLIED`, `REVIEWING`, `SHORTLISTED`, `INTERVIEW_SCHEDULED`, `SELECTED`, `REJECTED`, `WITHDRAWN` |
| `cover_letter` | TEXT | NULLABLE | Student application cover note |
| *Composite Unique* | (job_id, student_id) | UNIQUE | Prevents duplicate applications |

### 6. `interviews`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Interview record ID |
| `application_id` | BIGINT | NOT NULL, FK -> applications(id) | Associated candidate application |
| `mode` | VARCHAR(20) | NOT NULL | `ONLINE`, `OFFLINE` |
| `interview_date` | DATE | NOT NULL | Scheduled date |
| `interview_time` | TIME | NOT NULL | Scheduled time |
| `location_or_link`| VARCHAR(255) | NOT NULL | Meeting link (Teams/Meet) or venue room |
| `interviewer_name`| VARCHAR(100) | NULLABLE | Assigned interviewer / panel |
| `instructions` | TEXT | NULLABLE | Round guidelines for candidate |
| `status` | VARCHAR(30) | NOT NULL, DEFAULT 'SCHEDULED' | `SCHEDULED`, `COMPLETED`, `CANCELLED`, `RESCHEDULED` |

---

## 11. Core Algorithms & Business Workflows

### 11.1 Student Eligibility & Application Algorithm
```
FUNCTION applyForJob(studentId, jobId, coverLetter):
    job = findJobById(jobId)
    IF job.status != ACTIVE OR job.deadline < currentDate THEN
        THROW InvalidOperationException("Job is closed or expired")
    
    IF applicationExists(jobId, studentId) THEN
        THROW InvalidOperationException("Duplicate application detected")
    
    resume = findLatestResume(studentId)
    IF resume IS NULL THEN
        THROW InvalidOperationException("Resume must be uploaded first")
    
    profile = findStudentProfile(studentId)
    IF profile.cgpa < job.minCgpa THEN
        THROW InvalidOperationException("CGPA criteria not met")
    
    IF job.allowedBranches IS NOT EMPTY THEN
        IF NOT branchMatch(profile.branch, job.allowedBranches) THEN
            THROW InvalidOperationException("Branch not eligible")
    
    application = CREATE Application(job, student, resume, coverLetter, status=APPLIED)
    SAVE application
    CREATE ApplicationStatusHistory(application, null, APPLIED, "Submitted", student)
    SEND Notification(job.recruiter, "New Applicant", application.id)
    RETURN application
END FUNCTION
```

### 11.2 Secure Resume Upload & Path Traversal Prevention
```
FUNCTION storeResumeFile(multipartFile, student):
    IF multipartFile.size > 5MB THEN
        THROW MaxUploadSizeExceededException()
    
    extension = extractExtension(multipartFile.originalFilename)
    IF extension NOT IN [".pdf", ".doc", ".docx"] THEN
        THROW InvalidOperationException("Unsupported file type")
    
    IF multipartFile.originalFilename CONTAINS ".." THEN
        THROW InvalidOperationException("Path traversal attempt detected")
    
    uniqueFilename = UUID.randomUUID() + extension
    targetPath = uploadDirectory.resolve(uniqueFilename)
    Files.copy(multipartFile.inputStream, targetPath, REPLACE_EXISTING)
    
    resume = CREATE Resume(student, originalFilename, uniqueFilename, contentType, size)
    SAVE resume
    RETURN resume
END FUNCTION
```

---

## 12. Test Plan & Test Execution Summary

| Test Case ID | Test Category | Target Component | Scenario Description | Expected Outcome | Execution Result |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **TC-AUTH-01** | Security | `AuthController` | Student registration with valid data | User created in DB with BCrypt password hash | **PASSED** |
| **TC-AUTH-02** | Security | `StudentService` | Registration with mismatched passwords | `InvalidOperationException` thrown | **PASSED** |
| **TC-AUTH-03** | Security | `StudentService` | Registration with duplicate email | Prevented with user-friendly error message | **PASSED** |
| **TC-AUTH-04** | Security | `UserService` | Password reset token generation & redemption | Token expires after 1 hour, single use only | **PASSED** |
| **TC-SEC-01** | Authorization | `SecurityConfig` | Anonymous user accessing `/student/dashboard` | Redirected to `/login` | **PASSED** |
| **TC-SEC-02** | Authorization | `SecurityConfig` | Student attempting to access `/admin/dashboard` | HTTP 403 Forbidden page rendered | **PASSED** |
| **TC-JOB-01** | Business Rule | `JobService` | Recruiter posting job with approved company | Job created with status `ACTIVE` | **PASSED** |
| **TC-JOB-02** | Business Rule | `JobService` | Recruiter posting job with unapproved company | Blocked with warning notification | **PASSED** |
| **TC-APP-01** | Business Rule | `ApplicationService`| Eligible student applying for active job | Application created with status `APPLIED` | **PASSED** |
| **TC-APP-02** | Business Rule | `ApplicationService`| Student applying twice to the same job | Duplicate rejected with error | **PASSED** |
| **TC-APP-03** | Business Rule | `ApplicationService`| Student with CGPA 7.5 applying for CGPA 8.0 cutoff | Blocked by automated eligibility check | **PASSED** |
| **TC-INT-01** | Notification | `InterviewService` | Recruiter schedules interview round | Application moves to `INTERVIEW_SCHEDULED` and student receives notification | **PASSED** |
| **TC-REP-01** | Reporting | `ReportService` | Admin queries company placement summary | Aggregated stats calculated via JdbcTemplate | **PASSED** |

---

## 13. Known Limitations & Future Enhancements
### Limitations
- File uploads are currently written to local server disk storage rather than cloud object storage (e.g. AWS S3).
- Notifications are displayed in-app only rather than dispatched via SMTP or SMS gateway.

### Future Enhancements
- Integration of Spring AI for resume keyword matching against job requirements.
- Integration with external calendar APIs (Google Calendar / Microsoft Outlook) for interview sync.
- NAAC / NBA automated PDF placement report generation with digital signatures.

---

## 14. Conclusion & References
The **Campus Placement and Internship Portal** fulfills all minor project criteria for the Master of Computer Applications (MCA) curriculum. Built upon industry-standard enterprise patterns, the system balances user experience, strict server-side authorization, automated business logic verification, and relational database integrity.

### Academic & Technical References
1. *Spring Boot Reference Documentation (v3.3.5)* - VMware Tanzu.
2. *Spring Security Architecture & OAuth2 Guidelines* - Spring.io.
3. *High-Performance Java Persistence* - Vlad Mihalcea.
4. *Database System Concepts (7th Edition)* - Silberschatz, Korth, Sudarshan.
5. *Clean Architecture: A Craftsman's Guide to Software Structure and Design* - Robert C. Martin.
