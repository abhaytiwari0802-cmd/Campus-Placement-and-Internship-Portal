# 🎓 Campus Placement and Internship Portal

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://www.oracle.com/java/)
[![Security](https://img.shields.io/badge/Spring%20Security-6.3.4-blue.svg)](https://spring.io/projects/spring-security)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)](https://www.mysql.com/)
[![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3-purple.svg)](https://getbootstrap.com/)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.1-green.svg)](https://www.thymeleaf.org/)

An enterprise-grade, role-based, database-driven web application engineered to digitize collegiate campus recruitment drives. Designed to replace fragmented spreadsheets, manual resume collection, and uncoordinated email threads, the portal delivers a centralized, transparent platform connecting **Students**, **Company Recruiters**, and **University Training & Placement Officers (TPO / Admins)**.

---

## 📑 Table of Contents
- [Key Features](#-key-features)
  - [1. Student Portal](#1-student-portal)
  - [2. Recruiter Portal](#2-recruiter-portal)
  - [3. TPO / Administrator Control Hub](#3-tpo--administrator-control-hub)
- [Enterprise Architecture](#-enterprise-architecture)
- [Technology Stack](#-technology-stack)
- [Default Demo Credentials](#-default-demo-credentials)
- [System Requirements & Prerequisites](#-system-requirements--prerequisites)
- [Database Setup & Configuration](#-database-setup--configuration)
- [How to Build and Run](#-how-to-build-and-run)
  - [Option A: Running from Eclipse / Spring Tool Suite (STS)](#option-a-running-from-eclipse--spring-tool-suite-sts)
  - [Option B: Running from Terminal / Command Line](#option-b-running-from-terminal--command-line)
- [Project Directory Structure](#-project-directory-structure)
- [Automated Business Logic & Security](#-automated-business-logic--security)
- [Troubleshooting & FAQs](#-troubleshooting--faqs)

---

## ✨ Key Features

### 1. Student Portal
- **Profile Completion Scoring:** Dynamically calculates profile completeness (academic marks, branch, skills, LinkedIn, GitHub, project highlights).
- **Resume Upload & Versioning:** Secure local filesystem storage for PDF/DOCX resumes (up to 5MB) with active resume auto-binding.
- **Job Discovery & Search:** Multi-criteria filtering by keyword, job type (`FULL_TIME`, `INTERNSHIP`, `PART_TIME`), and location.
- **Automated Eligibility Engine:** Visual badges (`Eligible` vs `Ineligible`) based on real-time validation of minimum CGPA thresholds and allowable academic branches.
- **End-to-End Application Tracking:** Transparent status progression timeline (`APPLIED` → `UNDER_REVIEW` → `SHORTLISTED` → `INTERVIEW_SCHEDULED` → `SELECTED` / `REJECTED`).
- **Interview Hub:** Centralized calendar showing upcoming interview rounds, interview modes (Online, In-Person, Telephonic), meeting links, and interviewer instructions.
- **Voluntary Withdrawal:** Allows candidates to withdraw applications before evaluation stages finalize.

### 2. Recruiter Portal
- **Company Association:** Onboard company profiles (industry, website, description, corporate address) subject to admin accreditation.
- **Job Lifecycle Governance:** Post vacancies, set application deadlines, define minimum CGPA, specify target branches, and toggle postings between `ACTIVE` and `CLOSED`.
- **Applicant Review Pipeline:** Review applicant profiles, download resumes, read candidate cover notes, and inspect academic records.
- **Application Status Updates:** Move candidates through evaluation stages with timestamped remarks recorded in an audit trail.
- **Interview Scheduling:** Schedule interview rounds, set dates/times, assign interviewers, provide Google Meet/Zoom links or physical room numbers, and trigger automated candidate alerts.

### 3. TPO / Administrator Control Hub
- **Placement Analytics & Reporting:** Direct SQL aggregation queries powered by Spring `JdbcTemplate`:
  - University-wide application conversion funnel.
  - Branch-wise placement percentages and placed student counts.
  - Company and job drive breakdown metrics.
- **Company Accreditation:** Verify and approve/reject recruiter-registered company profiles.
- **User Governance:** Inspect registered students, recruiters, and administrative accounts with activation/suspension toggles.
- **Campus Announcements:** Broadcast critical placement notices and drive deadlines targeted to `ALL`, `STUDENTS`, or `RECRUITERS` with priority badges (`URGENT`, `HIGH`, `NORMAL`).
- **Immutable Audit Logging:** System-wide tracking of critical administrative, recruiter, and candidate actions with timestamps and IP addresses.

---

## 🏛️ Enterprise Architecture

The system implements the **3-Tier Model-View-Controller (MVC)** architectural pattern:

```
               +-------------------------------------------------------+
               |        Presentation Tier: Thymeleaf + Bootstrap 5      |
               |       (Responsive UI, CSRF Tokens, Dynamic Navbars)    |
               +---------------------------+---------------------------+
                                           | HTTP Requests / Form Posts
                                           v
               +-------------------------------------------------------+
               |   Application Tier: Spring Boot 3.3.5 / Spring MVC     |
               |  - Spring Security 6 (RBAC, BCrypt, Session Control)  |
               |  - Controllers: Auth, Student, Recruiter, Admin       |
               |  - Business Services: Eligibility Engine, Workflows   |
               +---------------------------+---------------------------+
                                           | JPA / Hibernate / JdbcTemplate
                                           v
               +-------------------------------------------------------+
               |             Data Tier: MySQL 8 Relational DB          |
               |     (ACID Compliance, Foreign Keys, Audit Tables)     |
               +-------------------------------------------------------+
```

---

## 💻 Technology Stack

| Layer | Technology | Details |
| :--- | :--- | :--- |
| **Backend Framework** | Spring Boot 3.3.5 | Spring MVC, Spring Data JPA, Spring Security 6, Jakarta Validation |
| **Language** | Java 21 LTS | Compatible with JDK 21 / 22 / 26 |
| **Direct SQL Analytics** | Spring JDBC (`JdbcTemplate`) | High-speed aggregated relational placement analytics |
| **View Engine** | Thymeleaf 3.1 | Server-side template rendering with Spring Security dialect |
| **Frontend Styling** | Bootstrap 5.3 & Bootstrap Icons | Modern, mobile-responsive dark/light component styling |
| **Database** | MySQL 8.0+ | Relational storage with foreign key constraints & UTF-8 encoding |
| **File Storage** | Java NIO File Storage | Physical filesystem upload for resumes (`./uploads/resumes`) |
| **Build Tool** | Apache Maven 3.9+ | Dependency management and executable fat JAR packaging |

---

## 🔑 Default Demo Credentials

The database is pre-seeded on first run (`DataInitializer.java`) with realistic demo data:

| Role | Name | Email Address | Password |
| :--- | :--- | :--- | :--- |
| 🛡️ **TPO / Admin** | Dr. Alok Verma | `admin@campus.edu` | `Admin@123` |
| 🏢 **Recruiter (Google)** | Sundar Raman | `recruiter.google@tech.com` | `Recruiter@123` |
| 🏢 **Recruiter (Microsoft)** | Kavita Deshmukh | `recruiter.msft@microsoft.com` | `Recruiter@123` |
| 🏢 **Recruiter (TCS)** | Ananya Sen | `recruiter.tcs@tcs.com` | `Recruiter@123` |
| 🎓 **Student (MCA)** | Rahul Sharma | `student.rahul@campus.edu` | `Student@123` |
| 🎓 **Student (B.Tech CSE)**| Priya Patel | `student.priya@campus.edu` | `Student@123` |

---

## ⚙️ System Requirements & Prerequisites

- **Java Development Kit (JDK):** Version 21 (LTS) or higher installed and set on `JAVA_HOME`.
- **MySQL Server:** Version 8.0 or higher running on `localhost:3306`.
- **RAM:** Minimum 8 GB (16 GB recommended).
- **IDE:** Eclipse IDE for Enterprise Java / Spring Tool Suite (STS) / IntelliJ IDEA.

---

## 🗄️ Database Setup & Configuration

1. **Start MySQL Server** (via MySQL Workbench, Services, or XAMPP).
2. Open your MySQL client and ensure the database exists (or let Spring Boot auto-create it):
   ```sql
   CREATE DATABASE IF NOT EXISTS campus_placement_portal_one
     CHARACTER SET utf8mb4 
     COLLATE utf8mb4_unicode_ci;
   ```
3. Verify or adjust database credentials in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/campus_placement_portal_one?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=root
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
   portal.upload.dir=./uploads/resumes
   ```

---

## 🚀 How to Build and Run

### Option A: Running from Eclipse / Spring Tool Suite (STS)
1. Launch **Eclipse IDE**.
2. Go to **File** &rarr; **Import...** &rarr; **Existing Maven Projects**.
3. Browse to the project folder (`campusplacement1`) and click **Finish**.
4. Right-click the project root in Project Explorer &rarr; **Maven** &rarr; **Update Project...** (or press `Alt + F5`).
5. Right-click `com.portal.CampusPortalApplication.java` &rarr; **Run As** &rarr; **Spring Boot App** (or **Java Application**).
6. Open your web browser and navigate to:
   ```
   http://localhost:8080
   ```

### Option B: Running from Terminal / Command Line
1. Open PowerShell or Command Prompt in the project root directory.
2. Build and run using the Maven wrapper or installed Maven:
   ```bash
   mvn clean spring-boot:run
   ```
3. Or package into an executable JAR:
   ```bash
   mvn clean package -DskipTests
   java -jar target/campus-placement-portal-1.0.0.jar
   ```

---

## 📂 Project Directory Structure

```
campusplacement1/
├── pom.xml                                      # Maven dependencies & build configuration
├── uploads/resumes/                             # Uploaded candidate PDF/DOCX resumes
├── src/
│   ├── main/
│   │   ├── java/com/portal/
│   │   │   ├── CampusPortalApplication.java     # Spring Boot application entry point
│   │   │   ├── config/
│   │   │   │   ├── DataInitializer.java        # Initial database seeder (Demo accounts & jobs)
│   │   │   │   ├── MvcConfig.java              # Static resource & format configuration
│   │   │   │   └── SecurityConfig.java         # Spring Security 6 RBAC filter chain & BCrypt
│   │   │   ├── controller/
│   │   │   │   ├── AdminController.java        # TPO dashboard, reports, company & user governance
│   │   │   │   ├── AuthController.java         # Login, registration & password reset
│   │   │   │   ├── CareerPrepController.java   # Technical interview & placement preparation hub
│   │   │   │   ├── HomeController.java         # Landing page & public navigation
│   │   │   │   ├── RecruiterController.java    # Job postings, candidate evaluation & scheduling
│   │   │   │   └── StudentController.java      # Job search, applications, resumes & interviews
│   │   │   ├── dto/                            # Data Transfer Objects & validation request models
│   │   │   ├── exception/
│   │   │   │   └── GlobalExceptionHandler.java # Centralized @ControllerAdvice error handling
│   │   │   ├── model/                          # JPA Entities (User, Job, Application, Company, etc.)
│   │   │   ├── repository/                     # Spring Data JPA Repositories
│   │   │   ├── security/
│   │   │   │   ├── CustomAuthenticationSuccessHandler.java  # Role-based dashboard redirector
│   │   │   │   ├── CustomUserDetails.java                   # UserDetails implementation
│   │   │   │   └── CustomUserDetailsService.java            # Database-backed auth provider
│   │   │   └── service/
│   │   │       ├── ApplicationService.java     # Application state machine & duplicate prevention
│   │   │       ├── AuditLogService.java        # Immutable system audit logging
│   │   │       ├── CompanyService.java         # Corporate profile accreditation
│   │   │       ├── InterviewService.java       # Multi-mode interview scheduling
│   │   │       ├── JobService.java             # Automated Eligibility Engine & job search
│   │   │       ├── NotificationService.java    # In-app alerts & announcement notifications
│   │   │       ├── RecruiterService.java       # Recruiter profile & company association
│   │   │       ├── ReportService.java          # JdbcTemplate direct SQL aggregate analytics
│   │   │       ├── ResumeStorageService.java   # Physical file upload & validation
│   │   │       ├── StudentService.java         # Dual-entity student onboarding & scoring
│   │   │       └── UserService.java            # Account status & tokenized password reset
│   │   └── resources/
│   │       ├── application.properties          # Server port, MySQL connection & JPA config
│   │       ├── static/                         # CSS, JavaScript, images, and favicon
│   │       └── templates/                      # Thymeleaf server-side HTML views
│   │           ├── admin/                      # TPO dashboards, analytics reports, companies
│   │           ├── recruiter/                  # Recruiter dashboards, applicants, job creation
│   │           ├── student/                    # Student dashboards, jobs, profile, interviews
│   │           ├── fragments/                  # Reusable layouts, navbars, footers, alerts
│   │           └── error/                      # Custom 403, 404, and 500 error views
```

---

## 🧠 Automated Business Logic & Security

1. **Role-Based Access Control (RBAC):**
   - URL routes are strictly segregated (`/student/**`, `/recruiter/**`, `/admin/**`).
   - Unauthorized attempts automatically redirect to a customized `403 Access Denied` view.
2. **Automated Candidate Eligibility Engine:**
   - Evaluates whether `StudentProfile.cgpa >= Job.minCgpa`.
   - Performs case-insensitive matching between student's branch/course and job's comma-separated `allowedBranches`.
3. **Application State Machine:**
   - Enforces valid status transitions:
     $$\text{APPLIED} \longrightarrow \text{UNDER\_REVIEW} \longrightarrow \text{SHORTLISTED} \longrightarrow \text{INTERVIEW\_SCHEDULED} \longrightarrow \text{SELECTED / REJECTED}$$
   - Voluntary candidate withdrawal is restricted exclusively to early stages (`APPLIED` and `UNDER_REVIEW`).
4. **Duplicate Prevention:**
   - Atomic database constraint checks (`existsByJobAndStudent`) ensure a student cannot submit duplicate applications for the same opening.
5. **Secure Resume Handling:**
   - Strict extension (`.pdf`, `.doc`, `.docx`) and MIME-type validation.
   - Files are stored with unique UUID filenames on disk to eliminate path traversal vulnerabilities.

---

## ❓ Troubleshooting & FAQs

- **Q: `Access denied for user 'root'@'localhost'` on startup?**  
  *A:* Update the `spring.datasource.password` in `src/main/resources/application.properties` to match your local MySQL root password.
- **Q: Port 8080 already in use?**  
  *A:* Change `server.port=8080` to `server.port=8081` in `application.properties`, or terminate the lingering process on port 8080.
- **Q: Where are uploaded student resumes stored?**  
  *A:* Resumes are saved locally in the `./uploads/resumes` directory in the project root. This directory is automatically created on startup if it does not exist.
- **Q: How to reset database seed data?**  
  *A:* Drop and recreate the database `campus_placement_portal_one` in MySQL; the `DataInitializer` will automatically re-populate all default demo accounts and job drives on next launch.

---

## 📄 License & Attribution
Developed as an academic capstone / mini-project demonstrating full-stack enterprise Java engineering, secure RBAC architecture, and relational database optimization. Released under the **MIT License**.
