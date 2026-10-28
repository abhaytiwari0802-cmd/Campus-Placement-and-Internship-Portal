# Campus Placement and Internship Portal
## Eclipse IDE Setup, Database Configuration, and Execution Manual

This comprehensive guide provides step-by-step instructions to import, configure, run, and test the **Campus Placement and Internship Portal** using **Eclipse IDE for Enterprise Java and Web Developers**, **Java 26 / 21**, and **MySQL 8+**.

---

## 1. System Requirements & Prerequisites

Ensure the following tools are present on your system:
- **Operating System:** Windows 10/11, macOS, or Linux
- **IDE:** Eclipse IDE (2024-03 or newer with m2e Maven integration and Spring Tools 4 recommended)
- **Java Development Kit (JDK):** JDK 26 or JDK 21 (64-bit)
- **Database:** MySQL Server 8.0+ (running locally on port 3306)
- **Build Tool:** Apache Maven (bundled within Eclipse m2e plugin)
- **Web Browser:** Google Chrome, Microsoft Edge, or Mozilla Firefox

---

## 2. MySQL Database Setup

### Step 2.1: Verify MySQL Service
Open PowerShell or Command Prompt and ensure MySQL service is running:
```powershell
Get-Service MySQL*
```
Or start the service:
```powershell
net start MySQL80
```

### Step 2.2: Create the Database
Open MySQL Command Line Client or MySQL Workbench:
```sql
CREATE DATABASE IF NOT EXISTS campus_placement_portal_one 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

> [!NOTE]
> The database name specified in `src/main/resources/application.properties` is configured with `createDatabaseIfNotExist=true`. Spring Boot will automatically create the database and tables upon first launch if your user account has `CREATE` privileges.

### Step 2.3: Configure Database Credentials
Open `src/main/resources/application.properties`. You can either:
1. Provide credentials directly in `application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/campus_placement_portal_one?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=your_mysql_password
   ```
2. Or set environment variables without editing the file:
   - `DB_USERNAME=root`
   - `DB_PASSWORD=your_mysql_password`

---

## 3. Importing the Project into Eclipse IDE

### Step 3.1: Open Eclipse Workspace
1. Launch **Eclipse IDE**.
2. Select your preferred workspace directory (e.g. `C:\Users\abhay\eclipse-workspace`) and click **Launch**.

### Step 3.2: Import as Existing Maven Project
1. In the Eclipse top menu, click **File** &rarr; **Import...**
2. In the Import wizard, expand **Maven** and select **Existing Maven Projects**. Click **Next >**.
3. In **Root Directory**, click **Browse...** and navigate to your extracted project folder:
   `C:\Users\abhay\Downloads\campusplacement1`
4. Eclipse will scan and detect `/pom.xml com.portal:campus-portal:1.0.0:jar`.
5. Ensure the checkbox next to `pom.xml` is **checked**.
6. Click **Finish**. Eclipse will begin importing the project and resolving dependencies.

---

## 4. Configuring JDK 26 in Eclipse

1. In Eclipse, right-click on the project `campus-portal` in the **Package Explorer** or **Project Explorer**.
2. Select **Properties** (or press `Alt + Enter`).
3. In the left panel, navigate to **Java Build Path** &rarr; **Libraries** tab.
4. If the JRE System Library shows an error or an incompatible version:
   - Select **JRE System Library** and click **Edit...**
   - Choose **Workspace default JRE** (or **Installed JREs...** and add JDK 26 from `C:\Program Files\Java\jdk-26` or `C:\Program Files\Common Files\Oracle\Java`).
   - Click **Finish**.
5. In the left panel, select **Java Compiler**:
   - Ensure **Compiler compliance level** matches your JDK version (21 or 26).
   - Ensure the checkbox **Store information about method parameters** is checked (required by Spring Framework 6).
6. Click **Apply and Close**.

---

## 5. Updating Maven Dependencies

If you notice any red markers or unresolved dependencies after importing:
1. In Eclipse **Package Explorer**, right-click the `campus-portal` project.
2. Select **Maven** &rarr; **Update Project...** (or press `Alt + F5`).
3. In the dialog, check **Force Update of Snapshots/Releases**.
4. Click **OK**. Wait for the bottom-right progress bar to finish (`Building workspace...`).

---

## 6. Running the Application

### Option A: Running as Spring Boot App (Recommended)
1. In Eclipse **Package Explorer**, locate:
   `src/main/java/com/portal/CampusPortalApplication.java`
2. Right-click `CampusPortalApplication.java`.
3. Select **Run As** &rarr; **Spring Boot App** (or **Java Application**).
4. Inspect the Eclipse **Console** tab at the bottom.
5. You should see the Spring Boot banner, Hibernate initialization, and the completion message:
   ```
   INFO ... [main] c.p.c.DataInitializer : Seeding initial database records for Campus Placement Portal...
   INFO ... [main] c.p.c.DataInitializer : Database successfully initialized with initial demonstration data.
   INFO ... [main] c.p.CampusPortalApplication : Started CampusPortalApplication in X.XX seconds
   ```

### Option B: Running via Eclipse Maven Plugin
1. Right-click on `pom.xml` &rarr; **Run As** &rarr; **Maven build...**
2. In **Goals**, enter: `spring-boot:run`
3. Click **Run**.

---

## 7. Accessing the Portal in Your Web Browser

Open your browser and navigate to:
```
http://localhost:8080/
```

### Pre-Seeded Demonstration Accounts

The portal includes realistic, pre-configured seed data ready for immediate testing and academic faculty demonstration:

| Role | Email Address | Password | Account Holder / Functionality |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin@campus.edu` | `Admin@123` | **Dr. Alok Verma** (TPO Head)<br>&bull; Master Dashboard<br>&bull; User Management<br>&bull; Company Accreditation<br>&bull; Broadcast Announcements<br>&bull; Placement Reports &amp; Diagnostics |
| **Recruiter** | `recruiter.google@tech.com` | `Recruiter@123` | **Sundar Raman** (Google India Lead)<br>&bull; Post &amp; Edit Vacancies<br>&bull; View Applicants &amp; Resumes<br>&bull; Shortlist &amp; Reject Candidates<br>&bull; Schedule Online Interviews |
| **Recruiter** | `recruiter.msft@microsoft.com` | `Recruiter@123` | **Kavita Deshmukh** (Microsoft IDC)<br>&bull; Azure Team Hiring<br>&bull; Schedule Teams Interviews |
| **Student** | `student.rahul@campus.edu` | `Student@123` | **Rahul Sharma** (MCA 8.85 CGPA)<br>&bull; Profile &amp; Resume Management<br>&bull; Browse &amp; Filter Jobs<br>&bull; Bookmark Jobs<br>&bull; Track Application Status &amp; Interviews |
| **Student** | `student.priya@campus.edu` | `Student@123` | **Priya Patel** (MCA 9.20 CGPA)<br>&bull; Placed at Google (Offers &amp; Status) |

---

## 8. Executing Automated Test Cases in Eclipse

1. In Eclipse **Package Explorer**, expand the `src/test/java` directory.
2. Right-click on the `src/test/java` folder (or individual test classes like `ApplicationServiceTest.java`).
3. Select **Run As** &rarr; **JUnit Test**.
4. The Eclipse **JUnit** panel will open, displaying a green bar indicating:
   - `UserServiceTest` - PASSED
   - `StudentServiceTest` - PASSED
   - `JobServiceTest` - PASSED
   - `ApplicationServiceTest` - PASSED
   - `InterviewServiceTest` - PASSED
   - `ReportServiceTest` - PASSED
   - `AuthControllerTest` - PASSED
   - `HomeControllerTest` - PASSED
   - `CareerPrepControllerTest` - PASSED
   - `StudentControllerTest` - PASSED
   - `RecruiterControllerTest` - PASSED
   - `AdminControllerTest` - PASSED

---

## 9. Troubleshooting Common Eclipse & Maven Issues

### Problem 1: Port 8080 Already in Use
**Symptom:** `Web server failed to start. Port 8080 was already in use.`
**Resolution:**
1. Open PowerShell and check what is running on port 8080:
   ```powershell
   Get-Process -Id (Get-NetTCPConnection -LocalPort 8080).OwningProcess
   ```
2. Either terminate that process, or edit `src/main/resources/application.properties` and change:
   ```properties
   server.port=8081
   ```
   Then access the portal at `http://localhost:8081/`.

### Problem 2: MySQL Access Denied for User 'root'@'localhost'
**Symptom:** `java.sql.SQLException: Access denied for user 'root'@'localhost' (using password: NO/YES)`
**Resolution:**
Ensure your MySQL root password in `src/main/resources/application.properties` matches your local MySQL installation password:
```properties
spring.datasource.password=your_correct_password
```

### Problem 3: "Method parameter names not discovered" Warning
**Symptom:** Spring Security or MVC complains about parameter reflection.
**Resolution:**
In Eclipse, go to **Project Properties** &rarr; **Java Compiler** &rarr; check **Store information about method parameters**. Then run **Project** &rarr; **Clean...**.
This is already configured in `pom.xml` with `<parameters>true</parameters>`.

### Problem 5: "Project campus-portal already exists" During Maven Import
**Symptom:** `Project campus-portal already exists. Add a version or custom suffix using "Name template" in "Advanced" settings.`
**Cause:** Eclipse already has a project named `campus-portal` registered in its current workspace metadata.
**Resolution (Choose Any Option):**
1. **Option A (Recommended):** In Eclipse's **Project Explorer**, locate the old `campus-portal`, right-click &rarr; **Delete** (make sure **"Delete project contents on disk" is UNCHECKED**). Then click **File** &rarr; **Import...** &rarr; **Existing Maven Projects** again.
2. **Option B (Name Template):** In the **Import Maven Projects** window, click the arrow next to **Advanced** at the bottom. In the **Name template** dropdown, select `[artifactId]-[version]` or type `[artifactId]-v1`, then click **Finish**.
3. **Option C (Updated Project Name):** The project descriptor has been updated to `campus-placement-portal` so you can re-browse and click **Finish** without naming collisions.

---

## 10. File Upload Directory Configuration

Resume uploads are automatically stored outside the public web root in `./uploads/resumes/` relative to the application launch directory.
- This directory is created automatically on application startup by `ResumeStorageService`.
- Allowed file formats: **PDF**, **DOC**, **DOCX**.
- Maximum file size: **5 MB** (configured via `spring.servlet.multipart.max-file-size=5MB`).
- Security: Files are assigned UUID filenames, and downloading enforces role ownership checks.
