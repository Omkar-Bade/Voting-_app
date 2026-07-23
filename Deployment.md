# VotingApp Production Deployment Guide (Render & Railway)

This guide documents the setup, configuration, and steps required to deploy the **VotingApp** MVC Java Servlet application on **Render** (via Docker) using **Railway MySQL** as the production database.

---

## 1. Render Deployment Guide

Follow these steps to deploy the application on Render:

### Step 1: Export the WAR File from Eclipse/STS
1. Open your project in **Spring Tool Suite (STS)**.
2. Right-click on the `VotingApp` project node -> **Export** -> **WAR file**.
3. Set the **Destination** directory to the root of your project workspace (saving it as `VotingApp.war`).
4. Ensure the exported file name is exactly `VotingApp.war`.

### Step 2: Push Changes to GitHub
Commit all files, including `VotingApp.war`, to your repository and push to GitHub:
```bash
git add .
git commit -m "Add Docker deployment configs and pre-built WAR"
git push origin main
```

### Step 3: Create Web Service on Render
1. Go to the [Render Dashboard](https://dashboard.render.com/) and log in.
2. Click **New +** -> **Web Service**.
3. Choose **Build and deploy from a Git repository** and select your `Voting-_app` repository.

### Step 4: Docker Runtime Detection & Configuration
1. **Name:** `voting-app`
2. **Region:** Choose a region close to your database/users.
3. **Branch:** `main`
4. **Runtime:** Select **Docker** (Render will automatically detect the root `Dockerfile` and build it).
5. **Instance Type:** Select the **Free** tier (or appropriate tier).

### Step 5: Configure Environment Variables
Before deploying, go to the **Environment** tab of your Render Web Service and add the required environment variables (see [Section 2: Environment Variable Guide](#2-environment-variable-guide) below).

### Step 6: Deploy & Monitor Logs
1. Click **Deploy Web Service**.
2. Go to the **Events** or **Logs** tab to monitor the build and deployment process.
3. Once the build finishes, Render will pull the image, start the container, run `start.sh` to bind the port, and launch Apache Tomcat.
4. When the log displays `Server startup in [X] milliseconds`, the application is live.

---

## 2. Environment Variable Guide

The application uses these environment variables to connect to the database and configure the runtime. You must set these in the Render Web Service dashboard under the **Environment** tab:

| Variable Name | Description | Source / Example Value |
| :--- | :--- | :--- |
| `DB_URL` | **Required.** MySQL connection URL pointing to the Railway instance. | Format: `jdbc:mysql://<railway-host>:<railway-port>/<railway-database>?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`<br>Example: `jdbc:mysql://roundhouse.proxy.rlwy.net:12345/railway?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `DB_USER` | **Required.** The username for the Railway MySQL database. | Provided by Railway (e.g., `root`) |
| `DB_PASSWORD` | **Required.** The password for the Railway MySQL database. | Provided by Railway (e.g., `aBcDeFg12345`) |
| `JAVA_OPTS` | **Optional.** Custom JVM startup arguments for performance tuning. | Default: `-Xms256m -Xmx512m -XX:+UseG1GC` |
| `PORT` | **Automated.** Port assigned dynamically by Render. | Do not configure manually. Render injects this, and `start.sh` reads it. |

---

## 3. Deployment Checklist

Verify each of these items before launching:
- [ ] `VotingApp.war` is exported to the root directory and is updated with the latest compiled Java classes.
- [ ] `db-init-railway.sql` has been executed on the Railway MySQL database instance.
- [ ] All table schemas (`users`, `candidates`) and seed records are verified in the Railway database.
- [ ] No local configuration passwords or JDBC strings are hardcoded in the codebase (verified in `DbUtil.java`).
- [ ] Render Web Service configuration has `Runtime` set to `Docker`.
- [ ] All environment variables (`DB_URL`, `DB_USER`, `DB_PASSWORD`) match the Railway connection settings exactly.
- [ ] GitHub repository is synchronized with the latest commit.

---

## 4. List of Project Changes

The following files were created in the repository root to support Docker containerization and Render deployment:

* **[db-init-local.sql](file:///d:/Study%20material/C-DAC/C-DAC%20Java/Rohan_sir_notes/Day20/VotingApp/db-init-local.sql):** Database initialization script with `CREATE DATABASE` and `USE` statements for local setup.
* **[db-init-railway.sql](file:///d:/Study%20material/C-DAC/C-DAC%20Java/Rohan_sir_notes/Day20/VotingApp/db-init-railway.sql):** Database initialization script optimized for Railway MySQL (omits `CREATE DATABASE` and `USE`).
* **[Dockerfile](file:///d:/Study%20material/C-DAC/C-DAC%20Java/Rohan_sir_notes/Day20/VotingApp/Dockerfile):** Defines the container image based on `tomcat:10.1-jdk21-temurin`, installs `ROOT.war`, and sets up execution of `start.sh`.
* **[.dockerignore](file:///d:/Study%20material/C-DAC/C-DAC%20Java/Rohan_sir_notes/Day20/VotingApp/.dockerignore):** Excludes source code, local build artifacts, database scripts, and IDE files to optimize Docker build speed and container size.
* **[start.sh](file:///d:/Study%20material/C-DAC/C-DAC%20Java/Rohan_sir_notes/Day20/VotingApp/start.sh):** Executable shell script that dynamically updates Tomcat's port binding in `server.xml` using the environment variable `$PORT` before starting the server.
* **[render.yaml](file:///d:/Study%20material/C-DAC/C-DAC%20Java/Rohan_sir_notes/Day20/VotingApp/render.yaml):** Infrastructure-as-code Blueprint file to set up the Render service with correct runtime and env var placeholders automatically.

---

## 5. List of Deployment Risks & Mitigation

| Risk | Description | Mitigation Strategy |
| :--- | :--- | :--- |
| **Windows CRLF line endings in start.sh** | Editing files on Windows causes carriage returns (`\r\n`), which fail to run in Linux containers, producing `\r: command not found` errors. | The `Dockerfile` includes a `sed` command: `RUN sed -i 's/\r$//' ...` which converts line endings to Unix format (`\n`) automatically during the build phase. |
| **Out of Memory (OOM) Crash** | The Render Free Tier has 512MB RAM. Running Java + Tomcat can exceed this, causing Render to kill the container. | We configured `JAVA_OPTS` to limit the heap size using `-Xms256m -Xmx512m` and use the memory-efficient G1 Garbage Collector (`-XX:+UseG1GC`) in `render.yaml`. |
| **Railway Connection Timeouts** | Database connections may drop if inactive, leading to JDBC errors. | `DbUtil.java` opens a fresh connection on every request (`getConnection()`) and uses try-with-resources in DAO classes to close connections/statements promptly. |
| **Stale WAR File** | Pushing code changes to GitHub without re-exporting the WAR file will cause old logic to be deployed. | **Always** re-export `VotingApp.war` from STS before making a commit/push. |
| **Database Dialect/Timezone Issues** | Railway MySQL is configured to UTC by default, which can cause date formatting issues with `dob` (date of birth). | The `DB_URL` environment variable includes `serverTimezone=UTC` to ensure correct calendar synchronization. |
