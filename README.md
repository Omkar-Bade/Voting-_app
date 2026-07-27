# 🗳️ Online Voting Application (VotingApp)

A full-stack, secure, web-based Online Voting System built with **Java Servlets**, **JDBC**, **MySQL**, **HTML5/CSS3**, and **Apache Tomcat**. The application features role-based access control for voters and administrators, secure password hashing using salted SHA-256, single-vote enforcement per user, and live statistics dashboards.

---

## 📌 Features

### 👤 Voter Features
- **User Registration & Login**: Secure account creation with validated input fields and credential verification.
- **Interactive Ballot Screen**: View all available election candidates, including their names and political parties.
- **Single Vote Enforcement**: Once a voter casts a vote, their voting status (`status = 1`) is locked, preventing duplicate voting.
- **Vote Duplicate Prevention**: Directs users who have already voted to an `Already Voted` notification page upon login or submittal.
- **Session Management**: Secure user session tracking across servlets with clean logout mechanisms.

### 🛡️ Admin Features
- **Admin Dashboard & Statistics**: Real-time summary of total election results, total votes cast per candidate, voter turnout, and winning candidate metrics.
- **Role-Based Access Control**: Differentiates between standard `voter` users and `admin` users during authentication.

### 🔒 Security & Architecture
- **Salted SHA-256 Hashing**: Passwords stored safely as `salt:hash` pairs using `PasswordUtil` to prevent plaintext leakage.
- **Decoupled Architecture**: Follows clean **MVC pattern** with Data Access Objects (`UserDao`, `CandidateDao`), Entity models, Servlets, and static/dynamic UI components.
- **Cloud Database Support**: Dynamic JDBC configuration (`DbUtil`) using environment variables for seamless deployment across local and cloud environments (Railway MySQL).

---

## 🛠️ Technology Stack

- **Backend**: Java 21, Java EE / Jakarta Servlets, JDBC API
- **Security**: SHA-256 with 16-byte random salt (`java.security.MessageDigest`, `SecureRandom`)
- **Frontend**: HTML5, CSS3 (Modern Glassmorphism / Gradient themes with `styles.css` & `landing.css`), JavaScript
- **Database**: MySQL 8.0 Server
- **Server**: Apache Tomcat 10.1 (Jakarta Servlet API)
- **Containerization & Cloud**: Docker (`tomcat:10.1-jdk21-temurin`), Render Web Services, Railway MySQL Database

---

## 📁 Project Architecture & Structure

```
VotingApp/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/sunbeam/votingapp/
│       │       ├── daos/              # Database Access Objects
│       │       │   ├── CandidateDao.java
│       │       │   └── UserDao.java
│       │       ├── entities/          # Data Models
│       │       │   ├── Candidate.java
│       │       │   └── User.java
│       │       ├── servlets/          # Controller Servlets
│       │       │   ├── CandidateServlet.java
│       │       │   ├── LoginServlet.java
│       │       │   ├── LogoutServlet.java
│       │       │   ├── RegisterServlet.java
│       │       │   ├── StatsServlet.java
│       │       │   └── VoteServlet.java
│       │       └── utils/             # Database Connection & Security Utilities
│       │           ├── DbUtil.java
│       │           └── PasswordUtil.java
│       └── webapp/                    # Web Application Content (HTML, CSS, WEB-INF)
│           ├── AlreadyVoted.html
│           ├── Logout.html
│           ├── Register.html
│           ├── Voted.html
│           ├── index.html
│           ├── landing.html
│           ├── landing.css
│           └── styles.css
├── db-init-local.sql                  # Database schema & seed data for local MySQL
├── db-init-railway.sql                # Database schema & seed data for Railway MySQL
├── Dockerfile                         # Container build configuration for Apache Tomcat
├── render.yaml                        # Infrastructure-as-code blueprint for Render deployment
├── start.sh                           # Dynamic port binding entrypoint script
├── VotingApp.war                      # Exported web archive for Tomcat deployment
└── README.md                          # Project documentation
```

---

## 📜 License & Acknowledgments

Developed as part of the **C-DAC Advanced Computing** curriculum (Sunbeam Institute).
