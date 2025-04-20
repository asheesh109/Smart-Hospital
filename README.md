# 🏥 Smart Hospital Management System

A modern Java-based Smart Hospital system designed to enhance hospital management through automation, real-time communication, and AI-powered assistance.

---

## 🚀 Features

- 🧑‍⚕️ **Doctor & Patient Management** – Add, update, and manage doctors and patients using an intuitive GUI built with Java Swing and AWT.
- 💬 **Real-time Chat System** – Uses `Socket.IO` to enable real-time communication between doctors and patients.
- 🤖 **AI Chatbot Integration** – Integrated with **Gemini API** to provide a chatbot that assists patients by answering health-related queries and guiding them through symptoms and diseases.
- 🗃️ **Database Integration** – All data (patients, doctors, appointments) is securely stored and managed in **MySQL**.
- 📋 **Appointment Scheduling** – Patients can schedule appointments with doctors through a user-friendly interface.
- 📈 **Modern & Clean UI** – Designed with a practical and clean UI for better user experience, inspired by real-world hospital systems.

---

## 🛠️ Tech Stack

| Layer              | Technology Used                |
|--------------------|-------------------------------|
| Language           | Java                           |
| UI Framework       | Java Swing & AWT               |
| Database           | MySQL                          |
| Real-time Comm.    | Socket.IO                      |
| AI Integration     | Gemini API (Google)            |

---

## 🧠 Gemini AI Chatbot

The chatbot leverages **Gemini API** to:
- Interact with patients through natural language.
- Guide them based on symptoms.
- Recommend actions or consultations.
- Reduce load on front desk and support staff.

---


## ⚙️ Setup Instructions

### 1. Clone the Repository


git clone https://github.com/asheesh109/Smart-Hospital.git
cd Smart-Hospital

### 2. MySQL Database Setup
Create a database: HMsystem

Import the SQL schema/tables from the provided .sql file (if available).

Update DB credentials in the Java code (if not using environment variables).

### 3. Install Dependencies
Ensure Java JDK is installed.

Install MySQL and configure the database.

Run npm install (if using Node.js for your Socket.IO server).

### 4. Run the Application
Launch the Java main class.

Start the Node.js server (if applicable for Socket.IO).

Chatbot requires a valid Gemini API key – add your key in the designated config file or environment variable.

### 📁 Folder Structure
bash
Copy
Edit
Smart-Hospital/
│
├── src/                      # Java source code
├── chat/                     # Socket.IO-related files (if any)
├── ui/                       # Java Swing/AWT UI panels
├── db/                       # Database interaction files (JDBC)
├── assets/                   # Images, icons, etc.
├── chatbot/                  # Gemini chatbot logic
├── package.json              # For Socket.IO backend (if used)
├── README.md
└── ...
### 📌 Future Improvements
Add patient medical history and report uploads.

Implement token-based authentication.

Doctor dashboard with analytics and charts.

Admin panel to manage hospital records.

### 🙋‍♂️ Author
Ashish Parab
Feel free to connect or raise an issue if you find any bugs or want to contribute!

📄 License
This project is open-source and free to use for educational purposes.


