# PSP Computer-Based Test (CBT) System

A complete, self-hosted exam platform: admin login, student login by matric
number, course/exam/question management, a timed exam interface with a
question navigator, auto-scoring, grading, a results dashboard with CSV
export, and a candidate reset tool — themed around your institution's crest.

Built with **Java 17, Spring Boot, Thymeleaf, and MySQL**. This guide assumes
nothing — follow it top to bottom and you'll have it running.

---

## What's in this version

- 🎓 Student login by matric number, admin login by username/password
- 🏫 Courses → Exams → Questions, fully admin-managed
- ⏱ Server-side timed exams (refreshing or closing the tab can't extend time)
- 🧭 Question navigator with answered/unanswered indicators
- ✅ Auto-scoring and letter grading on submit
- 📊 Results dashboard — search, filter by exam, **export to CSV**
- ♻️ **Reset tool** — clear a candidate's attempt (in-progress or completed)
  and let them retake it, no manual database work required
- 🖼 Institution logo on the home page and every header
- 🎨 A consistent design system themed on your crest's colors (crimson,
  gold, charcoal), with icons throughout
- 📱 Responsive — usable on a phone or tablet, not just a desktop browser

---

## 1. Install the tools (one-time setup — Ubuntu/Debian Linux)

If you're on Fedora/RHEL, swap `apt install` for `sudo dnf install`; on
Arch, `sudo pacman -S`.

### 1.1 Java Development Kit (JDK) 17
```bash
sudo apt update
sudo apt install openjdk-17-jdk
java -version   # should mention "17"
```

### 1.2 Apache Maven
```bash
sudo apt install maven
mvn -version
```
If `mvn` still isn't found in a specific terminal after installing (this
happens in sandboxed terminals like Flatpak VS Code), use the full path
Maven printed in `mvn -version` from a regular terminal instead, e.g.
`/usr/share/maven/bin/mvn spring-boot:run`, or just run the app from your
regular system terminal rather than the IDE's built-in one.

### 1.3 MySQL Server
```bash
sudo apt install mysql-server
sudo systemctl enable --now mysql
sudo systemctl status mysql        # should say "active (running)"
sudo mysql
```
Inside the MySQL prompt:
```sql
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'YourPassword';
FLUSH PRIVILEGES;
EXIT;
```

### 1.4 VS Code + extensions
```bash
sudo snap install code --classic
```
Install these extensions from the Extensions panel:
- **Extension Pack for Java** (Microsoft)
- **Spring Boot Extension Pack** (VMware/Broadcom)

---

## 2. Get the project open

1. Unzip the project folder anywhere.
2. VS Code → **File → Open Folder** → select it.
3. Wait for "Importing Java projects" to finish (needs internet — it's
   downloading the libraries the project depends on).

---

## 3. Configure the database connection

Open `src/main/resources/application.properties` and set your MySQL
password:
```properties
spring.datasource.password=YourPassword
```
The `psp_cbt` database and all its tables are created automatically the
first time you run the app — you don't need to create anything manually in
MySQL.

**Safer alternative** (keeps the password out of the file entirely):
```bash
export SPRING_DATASOURCE_PASSWORD='YourPassword'
```
Run this in the same terminal before starting the app, and leave the
`application.properties` line unset.

---

## 4. Add your institution logo

Save your logo image as:
```
src/main/resources/static/images/logo.jpg
```
It's already wired into the home page, the admin header, the student
header, the login pages, and the result screen. Swap the file and refresh
your browser — no code changes needed. (If your logo is a `.png`, either
rename it to `logo.jpg`, or find-and-replace `logo.jpg` → `logo.png` across
the templates in `src/main/resources/templates/`.)

---

## 5. Run it

**Option A — VS Code Run button:** open
`src/main/java/com/psp/cbt/CbtApplication.java` and click the **▶ Run**
link above `main`.

**Option B — terminal:**
```bash
cd psp-cbt-mvp
mvn spring-boot:run
```

Watch for:
```
Tomcat started on port 8080
...
Default admin created:
  username: admin
  password: admin123
```
Then open **http://localhost:8080**

That admin account is seeded once, the very first time the app runs
against an empty database.

---

## 6. Walkthrough

1. **Admin Login** (`admin` / `admin123`) → change this password mentally
   for anything beyond a demo; there's no self-service password change
   screen yet, so update it directly in the database if needed.
2. **Courses** → Add Course.
3. **Exams** → Create Exam → pick the course, set duration, status Active.
4. On the Exams list, **Questions** → add a few, marking the correct option.
5. **Students** → Add Student → matric number is generated automatically.
6. Log out → **Student Login** with that matric number → take the exam.
7. Back in Admin → **Results**: view scores, filter by exam, **Export CSV**,
   or **Reset** a candidate's attempt if they started by mistake or need a
   retake — this works whether they've submitted or are still mid-exam.

---

## 7. Does it need the internet to run?

**The app itself: no.** Once running, Java/MySQL/the exam logic/timer/
scoring are entirely local — students on the same local network as your
server can take exams with no internet connection at all.

**The styling and icons: currently yes.** Bootstrap, Bootstrap Icons, and
their JavaScript are loaded from a CDN (`cdn.jsdelivr.net`) in every page's
`<head>`. If the exam venue has no internet, the app still *works*
(buttons submit, forms save) but will render unstyled. If you need a fully
offline-capable exam hall, the fix is downloading those files into
`src/main/resources/static/` once and pointing the `<link>`/`<script>` tags
at the local copies instead of the CDN — ask if you'd like this done.

---

## 8. Common errors and fixes

| Symptom | Cause | Fix |
|---|---|---|
| `Connection refused` on startup | MySQL isn't running | `sudo systemctl start mysql` |
| `Access denied for user 'root'` | Wrong password | Re-check `application.properties` or your exported env var |
| `Port 8080 was already in use` | Something else is using it | Stop it, or set `server.port=8081` |
| `mvn: command not found` in VS Code terminal only | VS Code (esp. Flatpak) has a different PATH than your system terminal | Run `mvn spring-boot:run` from your normal terminal instead |
| Styling looks broken/unstyled | No internet reaching the CDN | See section 7 |
| Whitelabel Error Page | An exception happened server-side | Check the terminal output — the real Java stack trace prints there |

---

## 9. Project structure

```
Browser → Controller → Repository → MySQL
           (routes)     (JPA/Hibernate)
```
- `entity/` — one class per database table
- `repository/` — Spring auto-generates the SQL from these interfaces
- `service/` — grading logic, matric number building
- `controller/` — handles URLs and page routing
- `interceptor/` — login checks for `/admin/**` and `/student/**`
- `resources/templates/` — the HTML pages (Thymeleaf)
- `resources/static/css/style.css` — the whole design system (colors,
  fonts, card/button/table styling) — edit this one file to re-theme the
  entire app
- `resources/static/images/logo.jpg` — your institution logo
- `resources/static/js/exam-timer.js` — exam countdown, navigation, and
  answer-saving logic

---

## 10. Deploying online (optional)

If you want this reachable outside your local network, Render is a
reasonable free option for the **app itself**, but Render's free tier only
includes managed **PostgreSQL**, not MySQL — hosting the MySQL database
for free means either switching the app to PostgreSQL or pointing at an
external free MySQL provider. This is a separate, involved step (driver
swap, connection config) — happy to walk through it when you're ready to
deploy rather than demo locally.

---

## 11. What's simplified (worth mentioning in a defense/demo)

- School/department/programme are text fields, not separate management
  screens — still fully admin-editable, nothing hard-coded in Java.
- Exam availability is a single Active/Inactive toggle, not a date/time
  window.
- Grading scale lives in one file (`GradeService.java`) rather than an
  admin settings screen.
- No CSV *import* for bulk student upload yet (export is included).

None of these require a redesign to add later — the architecture (entity →
repository → service → controller) already supports it.