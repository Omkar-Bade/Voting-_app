# 🗳️ Online Voting Application (VotingApp)

A full-stack, secure, web-based Online Voting System built with **Java Servlets**, **JDBC**, **MySQL**, **HTML5/CSS3**, and **Apache Tomcat**. The application features role-based access control for voters and administrators, secure password hashing using salted SHA-256, single-vote enforcement per user, and live statistics dashboards.

---

## 🎯 Real-World Problem Statement

In traditional paper-based or unmanaged election systems (such as university student elections, organizational board polls, or municipal voting), election committees face critical security, efficiency, and integrity challenges:

1. **Voter Fraud & Duplicate Voting**: Lack of centralized, real-time identity locking allows individuals to cast multiple ballots or vote on behalf of absent members.
2. **Credential Vulnerability**: Insecure storage of voter identity information and plain-text passwords leaves systems vulnerable to data breaches and identity theft.
3. **Manual & Error-Prone Vote Tallying**: Physical vote counting requires significant manual labor, is time-consuming, and carries a high risk of human calculation errors or deliberate tampering.
4. **Lack of Transparency & Real-Time Analytics**: Election authorities lack live visibility into turnout rates, candidate standings, and statistical breakdowns during an active poll.

### 💡 The Solution

**VotingApp** provides a modern, secure, and automated digital polling system:
- **Enforced Single-Vote Policy**: Atomically locks each voter's account (`status = 1`) immediately after ballot submittal, preventing duplicate votes.
- **Cryptographic Credential Security**: Utilizes salted SHA-256 hashing (`salt:hash`) via `PasswordUtil` to protect user credentials against unauthorized disclosure.
- **Instant & Automated Analytics**: Provides an administrator dashboard that dynamically tallies votes, calculates candidate leads, and tracks turnout metrics in real time with zero human calculation error.

---

## 🚀 Use Cases

The **Online Voting Application** is designed to provide secure, transparent, and single-vote digital balloting across various real-world scenarios:

### 🎓 1. Educational Institutions & Student Government
- **Student Council & Class Representative Elections**: Enables universities, colleges, and schools to conduct secure annual student elections digitally.
- **Club & Society Leadership Polls**: Allows student tech clubs, cultural societies, and sports committees to vote for leadership positions.
- **Key Advantage**: Eliminates paper ballots, speeds up tallying, and strictly prevents duplicate voting.

### 🏢 2. Corporate & Organizational Governance
- **Board of Directors & Committee Voting**: Provides a tamper-resistant platform for board members and shareholders to vote on resolutions or executive appointments.
- **Internal Employee Recognition**: Facilitates transparent workplace polling for "Employee of the Month", team leadership awards, or innovation contests.
- **Key Advantage**: Real-time admin statistics dashboard (`StatsServlet`) provides instant election results without human calculation error.

### 🏘️ 3. Housing Societies & Non-Profit Organizations
- **Homeowners Association (HOA) Board Polls**: Facilitates voting for residential society management committees, maintenance proposals, and community initiatives.
- **NGO & Trustee Elections**: Enables non-profit organizations and charitable trusts to elect board members and approve strategic decisions.
- **Key Advantage**: Accessible web interface allows remote or busy members to cast votes effortlessly.

### 📚 4. Academic & Enterprise Java Learning Reference
- **Java Web Architecture Training**: Serves as a practical reference implementation for **C-DAC** students and developers learning Jakarta Servlets, JDBC DAO pattern, MVC design, and session management.
- **Security & Cloud Deployment Practice**: Demonstrates best practices for password security (salted SHA-256 via `PasswordUtil`), Docker containerization, and cloud deployment (Render Web Services & Railway MySQL).

### 🗳️ 5. Trade Unions & Professional Associations
- **Association Member Elections**: Suitable for local chapters of professional bodies (e.g., medical associations, teacher unions, bar associations) to select office bearers.
- **Key Advantage**: Cryptographic credential security (`salt:hash`) safeguards voter identity while role-based access control separates voters from administrators.

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
