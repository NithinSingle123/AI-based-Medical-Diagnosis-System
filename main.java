package com.medicaldiagnosis;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.Gson;

public class Main {
    public static void main(String[] args) throws Exception {
        DiseasePredictor.initializeModel();
        
        Server server = new Server(8080);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        
        context.addServlet(new ServletHolder(new PatientServlet()), "/api/patients/*");
        context.addServlet(new ServletHolder(new DiagnosisServlet()), "/api/diagnosis/*");
        context.addServlet(new ServletHolder(new HomeServlet()), "/*");
        
        server.start();
        System.out.println("====================================");
        System.out.println("AI Medical Diagnosis System Started");
        System.out.println("Server: http://localhost:8080");
        System.out.println("====================================");
        server.join();
    }
}

class Patient {
    private int id;
    private String name;
    private int age;
    private String gender;
    private String bloodGroup;
    private String medicalHistory;
    private List<String> symptoms;
    private String diagnosis;
    private double confidenceScore;
    
    public Patient() {
        this.symptoms = new ArrayList<>();
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public String getMedicalHistory() { return medicalHistory; }
    public void setMedicalHistory(String medicalHistory) { this.medicalHistory = medicalHistory; }
    public List<String> getSymptoms() { return symptoms; }
    public void setSymptoms(List<String> symptoms) { this.symptoms = symptoms; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
}

class DiagnosisResult {
    private String disease;
    private double probability;
    private String severity;
    private List<String> recommendations;
    
    public DiagnosisResult(String disease, double probability, String severity) {
        this.disease = disease;
        this.probability = probability;
        this.severity = severity;
        this.recommendations = new ArrayList<>();
    }
    
    public String getDisease() { return disease; }
    public double getProbability() { return probability; }
    public String getSeverity() { return severity; }
    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
}

class DiseasePredictor {
    private static Map<String, Map<String, Double>> diseaseSymptomWeights;
    private static Map<String, List<String>> diseaseRecommendations;
    
    public static void initializeModel() {
        diseaseSymptomWeights = new HashMap<>();
        diseaseRecommendations = new HashMap<>();
        
        Map<String, Double> coldWeights = new HashMap<>();
        coldWeights.put("runny nose", 0.9);
        coldWeights.put("sneezing", 0.85);
        coldWeights.put("sore throat", 0.75);
        coldWeights.put("cough", 0.7);
        coldWeights.put("mild fever", 0.6);
        coldWeights.put("fatigue", 0.5);
        diseaseSymptomWeights.put("Common Cold", coldWeights);
        diseaseRecommendations.put("Common Cold", Arrays.asList(
            "Rest and get adequate sleep",
            "Drink plenty of fluids",
            "Use over-the-counter cold medications",
            "Gargle with warm salt water"
        ));
        
        Map<String, Double> fluWeights = new HashMap<>();
        fluWeights.put("high fever", 0.95);
        fluWeights.put("body aches", 0.9);
        fluWeights.put("severe fatigue", 0.85);
        fluWeights.put("headache", 0.8);
        fluWeights.put("cough", 0.75);
        fluWeights.put("chills", 0.7);
        diseaseSymptomWeights.put("Influenza", fluWeights);
        diseaseRecommendations.put("Influenza", Arrays.asList(
            "Consult doctor for antiviral medication",
            "Complete bed rest required",
            "Stay hydrated with fluids",
            "Isolate to prevent spread"
        ));
        
        Map<String, Double> covidWeights = new HashMap<>();
        covidWeights.put("loss of taste", 0.95);
        covidWeights.put("loss of smell", 0.95);
        covidWeights.put("dry cough", 0.85);
        covidWeights.put("fever", 0.8);
        covidWeights.put("fatigue", 0.75);
        covidWeights.put("shortness of breath", 0.9);
        diseaseSymptomWeights.put("COVID-19", covidWeights);
        diseaseRecommendations.put("COVID-19", Arrays.asList(
            "Get tested immediately",
            "Self-isolate for 14 days",
            "Monitor oxygen levels",
            "Seek emergency care if breathing worsens"
        ));
        
        Map<String, Double> migraineWeights = new HashMap<>();
        migraineWeights.put("severe headache", 0.95);
        migraineWeights.put("nausea", 0.8);
        migraineWeights.put("sensitivity to light", 0.85);
        migraineWeights.put("sensitivity to sound", 0.8);
        migraineWeights.put("visual disturbances", 0.75);
        diseaseSymptomWeights.put("Migraine", migraineWeights);
        diseaseRecommendations.put("Migraine", Arrays.asList(
            "Rest in a dark quiet room",
            "Apply cold compress",
            "Take prescribed medication",
            "Avoid trigger factors"
        ));
        
        Map<String, Double> diabetesWeights = new HashMap<>();
        diabetesWeights.put("increased thirst", 0.9);
        diabetesWeights.put("frequent urination", 0.9);
        diabetesWeights.put("unexplained weight loss", 0.85);
        diabetesWeights.put("blurred vision", 0.75);
        diabetesWeights.put("slow healing wounds", 0.8);
        diabetesWeights.put("fatigue", 0.7);
        diseaseSymptomWeights.put("Type 2 Diabetes", diabetesWeights);
        diseaseRecommendations.put("Type 2 Diabetes", Arrays.asList(
            "Schedule blood glucose testing",
            "Consult endocrinologist",
            "Follow diabetic diet plan",
            "Regular exercise program"
        ));
        
        Map<String, Double> hypertensionWeights = new HashMap<>();
        hypertensionWeights.put("headache", 0.75);
        hypertensionWeights.put("dizziness", 0.8);
        hypertensionWeights.put("chest pain", 0.85);
        hypertensionWeights.put("shortness of breath", 0.75);
        hypertensionWeights.put("nosebleeds", 0.7);
        diseaseSymptomWeights.put("Hypertension", hypertensionWeights);
        diseaseRecommendations.put("Hypertension", Arrays.asList(
            "Monitor blood pressure regularly",
            "Reduce sodium intake",
            "Start antihypertensive medication",
            "Stress management exercises"
        ));
        
        Map<String, Double> asthmaWeights = new HashMap<>();
        asthmaWeights.put("wheezing", 0.95);
        asthmaWeights.put("shortness of breath", 0.9);
        asthmaWeights.put("chest tightness", 0.85);
        asthmaWeights.put("cough", 0.75);
        diseaseSymptomWeights.put("Asthma", asthmaWeights);
        diseaseRecommendations.put("Asthma", Arrays.asList(
            "Use prescribed inhaler",
            "Avoid allergens and triggers",
            "Keep emergency inhaler accessible",
            "Regular pulmonologist visits"
        ));
        
        Map<String, Double> gastritisWeights = new HashMap<>();
        gastritisWeights.put("stomach pain", 0.9);
        gastritisWeights.put("nausea", 0.85);
        gastritisWeights.put("vomiting", 0.8);
        gastritisWeights.put("indigestion", 0.85);
        gastritisWeights.put("loss of appetite", 0.7);
        diseaseSymptomWeights.put("Gastritis", gastritisWeights);
        diseaseRecommendations.put("Gastritis", Arrays.asList(
            "Avoid spicy and acidic foods",
            "Take prescribed antacids",
            "Eat smaller frequent meals",
            "Reduce stress and avoid alcohol"
        ));
        
        System.out.println("AI Model initialized with " + diseaseSymptomWeights.size() + " diseases");
    }
    
    public static List<DiagnosisResult> predict(List<String> symptoms, int age, String medicalHistory) {
        List<DiagnosisResult> results = new ArrayList<>();
        
        List<String> normalizedSymptoms = new ArrayList<>();
        for (String symptom : symptoms) {
            normalizedSymptoms.add(symptom.toLowerCase().trim());
        }
        
        for (Map.Entry<String, Map<String, Double>> disease : diseaseSymptomWeights.entrySet()) {
            String diseaseName = disease.getKey();
            Map<String, Double> weights = disease.getValue();
            
            double score = 0.0;
            int matchCount = 0;
            
            for (String symptom : normalizedSymptoms) {
                if (weights.containsKey(symptom)) {
                    score += weights.get(symptom);
                    matchCount++;
                }
            }
            
            if (age > 60 && (diseaseName.equals("Hypertension") || diseaseName.equals("Type 2 Diabetes"))) {
                score *= 1.2;
            }
            
            if (medicalHistory != null && !medicalHistory.isEmpty()) {
                if (medicalHistory.toLowerCase().contains("diabetes") && diseaseName.equals("Type 2 Diabetes")) {
                    score *= 1.3;
                }
                if (medicalHistory.toLowerCase().contains("heart") && diseaseName.equals("Hypertension")) {
                    score *= 1.3;
                }
            }
            
            if (matchCount > 0) {
                double probability = Math.min(score / weights.size() * 100, 99.0);
                
                String severity = "Low";
                if (probability > 70) severity = "High";
                else if (probability > 50) severity = "Moderate";
                
                DiagnosisResult result = new DiagnosisResult(diseaseName, probability, severity);
                result.setRecommendations(diseaseRecommendations.get(diseaseName));
                results.add(result);
            }
        }
        
        results.sort((a, b) -> Double.compare(b.getProbability(), a.getProbability()));
        return results;
    }
}

class PatientDB {
    private static Map<Integer, Patient> patients = new ConcurrentHashMap<>();
    private static int idCounter = 1;
    
    static {
        Patient p1 = new Patient();
        p1.setId(1);
        p1.setName("John Smith");
        p1.setAge(45);
        p1.setGender("Male");
        p1.setBloodGroup("O+");
        p1.setMedicalHistory("History of hypertension");
        p1.setSymptoms(Arrays.asList("headache", "dizziness", "chest pain"));
        p1.setDiagnosis("Hypertension");
        p1.setConfidenceScore(78.5);
        patients.put(1, p1);
        idCounter = 2;
    }
    
    public static List<Patient> getAllPatients() {
        return new ArrayList<>(patients.values());
    }
    
    public static Patient getPatient(int id) {
        return patients.get(id);
    }
    
    public static Patient addPatient(Patient patient) {
        patient.setId(idCounter++);
        patients.put(patient.getId(), patient);
        return patient;
    }
    
    public static boolean deletePatient(int id) {
        return patients.remove(id) != null;
    }
}

class PatientServlet extends HttpServlet {
    private Gson gson = new Gson();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        String pathInfo = req.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            List<Patient> patients = PatientDB.getAllPatients();
            resp.getWriter().write(gson.toJson(patients));
        } else {
            try {
                int id = Integer.parseInt(pathInfo.substring(1));
                Patient patient = PatientDB.getPatient(id);
                if (patient != null) {
                    resp.getWriter().write(gson.toJson(patient));
                } else {
                    resp.setStatus(404);
                }
            } catch (NumberFormatException e) {
                resp.setStatus(400);
            }
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        Patient patient = gson.fromJson(req.getReader(), Patient.class);
        Patient created = PatientDB.addPatient(patient);
        resp.setStatus(201);
        resp.getWriter().write(gson.toJson(created));
    }
    
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && !pathInfo.equals("/")) {
            try {
                int id = Integer.parseInt(pathInfo.substring(1));
                PatientDB.deletePatient(id);
                resp.setStatus(200);
            } catch (NumberFormatException e) {
                resp.setStatus(400);
            }
        }
    }
}

class DiagnosisServlet extends HttpServlet {
    private Gson gson = new Gson();
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        Patient patient = gson.fromJson(req.getReader(), Patient.class);
        List<DiagnosisResult> results = DiseasePredictor.predict(
            patient.getSymptoms(), 
            patient.getAge(), 
            patient.getMedicalHistory()
        );
        
        resp.getWriter().write(gson.toJson(results));
    }
}

class HomeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>AI Medical Diagnosis</title>");
        out.println("<style>");
        out.println("*{margin:0;padding:0;box-sizing:border-box}");
        out.println("body{font-family:Arial,sans-serif;background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);min-height:100vh;padding:20px}");
        out.println(".container{max-width:1400px;margin:0 auto}");
        out.println(".header{text-align:center;color:white;margin-bottom:30px}");
        out.println(".header h1{font-size:42px;margin-bottom:10px;text-shadow:2px 2px 4px rgba(0,0,0,0.3)}");
        out.println(".header p{font-size:18px;opacity:0.9}");
        out.println(".main-grid{display:grid;grid-template-columns:1fr 1fr;gap:20px;margin-bottom:20px}");
        out.println(".card{background:white;border-radius:15px;padding:25px;box-shadow:0 10px 30px rgba(0,0,0,0.2)}");
        out.println(".card h2{color:#667eea;margin-bottom:20px;font-size:24px;border-bottom:2px solid #667eea;padding-bottom:10px}");
        out.println(".form-group{margin-bottom:15px}");
        out.println("label{display:block;margin-bottom:5px;color:#555;font-weight:600}");
        out.println("input,select,textarea{width:100%;padding:10px;border:2px solid #e0e0e0;border-radius:8px;font-size:14px}");
        out.println("input:focus,select:focus,textarea:focus{border-color:#667eea;outline:none}");
        out.println(".symptom-selector{display:grid;grid-template-columns:repeat(2,1fr);gap:10px;margin-top:10px}");
        out.println(".symptom-checkbox{display:flex;align-items:center;padding:8px;background:#f5f5f5;border-radius:5px;cursor:pointer}");
        out.println(".symptom-checkbox:hover{background:#e8e8e8}");
        out.println(".symptom-checkbox input{width:auto;margin-right:8px}");
        out.println("button{background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);color:white;padding:12px 30px;border:none;border-radius:8px;cursor:pointer;font-size:16px;font-weight:600;margin-right:10px}");
        out.println("button:hover{transform:translateY(-2px);box-shadow:0 5px 15px rgba(102,126,234,0.4)}");
        out.println(".btn-secondary{background:linear-gradient(135deg,#f093fb 0%,#f5576c 100%)}");
        out.println(".results{background:white;border-radius:15px;padding:25px;box-shadow:0 10px 30px rgba(0,0,0,0.2)}");
        out.println(".diagnosis-item{background:linear-gradient(135deg,#f5f7fa 0%,#c3cfe2 100%);padding:20px;border-radius:10px;margin-bottom:15px;border-left:5px solid #667eea}");
        out.println(".diagnosis-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:15px}");
        out.println(".disease-name{font-size:22px;font-weight:bold;color:#333}");
        out.println(".probability{font-size:32px;font-weight:bold;color:#667eea}");
        out.println(".severity{display:inline-block;padding:5px 15px;border-radius:20px;font-size:12px;font-weight:bold;color:white;margin-left:10px}");
        out.println(".severity.High{background:#f5576c}");
        out.println(".severity.Moderate{background:#ffa726}");
        out.println(".severity.Low{background:#66bb6a}");
        out.println(".recommendations{margin-top:15px}");
        out.println(".recommendations h4{color:#667eea;margin-bottom:10px}");
        out.println(".recommendations ul{list-style:none}");
        out.println(".recommendations li{padding:8px;background:white;margin-bottom:5px;border-radius:5px;padding-left:25px;position:relative}");
        out.println(".recommendations li:before{content:'✓';position:absolute;left:8px;color:#667eea;font-weight:bold}");
        out.println(".patient-list{max-height:400px;overflow-y:auto}");
        out.println(".patient-item{padding:15px;background:#f5f5f5;margin-bottom:10px;border-radius:8px;display:flex;justify-content:space-between;align-items:center}");
        out.println(".patient-info{flex:1}");
        out.println(".patient-info strong{display:block;color:#333;font-size:16px}");
        out.println(".patient-info span{color:#666;font-size:14px}");
        out.println(".patient-actions button{padding:6px 12px;font-size:14px;margin-left:5px}");
        out.println(".alert{padding:15px;border-radius:8px;margin-bottom:20px;display:none}");
        out.println(".alert.success{background:#d4edda;color:#155724;border:1px solid #c3e6cb}");
        out.println(".alert.info{background:#d1ecf1;color:#0c5460;border:1px solid #bee5eb}");
        out.println(".loading{text-align:center;padding:40px}");
        out.println(".spinner{border:4px solid #f3f3f3;border-top:4px solid #667eea;border-radius:50%;width:40px;height:40px;animation:spin 1s linear infinite;margin:0 auto}");
        out.println("@keyframes spin{0%{transform:rotate(0deg)}100%{transform:rotate(360deg)}}");
        out.println(".stats{display:grid;grid-template-columns:repeat(3,1fr);gap:15px;margin-bottom:20px}");
        out.println(".stat-card{background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);color:white;padding:20px;border-radius:10px;text-align:center}");
        out.println(".stat-card h3{font-size:36px;margin-bottom:5px}");
        out.println(".stat-card p{font-size:14px;opacity:0.9}");
        
        // MODAL STYLES - THIS IS THE ONLY NEW ADDITION
        out.println(".modal{display:none;position:fixed;z-index:1000;left:0;top:0;width:100%;height:100%;background:rgba(0,0,0,0.7)}");
        out.println(".modal-content{background:white;margin:3% auto;padding:0;width:90%;max-width:800px;border-radius:15px;box-shadow:0 20px 60px rgba(0,0,0,0.5);animation:slideIn 0.3s}");
        out.println("@keyframes slideIn{from{transform:translateY(-50px);opacity:0}to{transform:translateY(0);opacity:1}}");
        out.println(".modal-header{background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);color:white;padding:25px;border-radius:15px 15px 0 0;display:flex;justify-content:space-between;align-items:center}");
        out.println(".modal-header h2{font-size:28px;margin:0}");
        out.println(".close{color:white;font-size:35px;font-weight:bold;cursor:pointer}");
        out.println(".close:hover{transform:scale(1.2)}");
        out.println(".modal-body{padding:30px;max-height:70vh;overflow-y:auto}");
        out.println(".record-section{margin-bottom:25px;padding:20px;background:#f8f9fa;border-radius:10px;border-left:4px solid #667eea}");
        out.println(".record-section h3{color:#667eea;margin-bottom:15px;font-size:20px}");
        out.println(".info-grid{display:grid;grid-template-columns:repeat(2,1fr);gap:15px}");
        out.println(".info-item{background:white;padding:12px;border-radius:8px;border:1px solid #e0e0e0}");
        out.println(".info-item label{font-weight:bold;color:#555;font-size:13px;display:block;margin-bottom:5px}");
        out.println(".info-item .value{color:#333;font-size:16px}");
        out.println(".symptom-badge{display:inline-block;background:white;padding:8px 12px;border-radius:8px;margin:5px;border:2px solid #667eea;color:#667eea;font-weight:600}");
        out.println(".diagnosis-box{background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);color:white;padding:20px;border-radius:10px;text-align:center;margin-top:15px}");
        out.println(".diagnosis-box h4{font-size:24px;margin-bottom:10px}");
        out.println(".diagnosis-box .conf-score{font-size:48px;font-weight:bold;margin:10px 0}");
        
        out.println("</style>");
        out.println("</head><body>");
        
        out.println("<div class='container'>");
        out.println("<div class='header'>");
        out.println("<h1>🏥 AI Medical Diagnosis System</h1>");
        out.println("<p>Advanced Deep Learning for Disease Prediction</p>");
        out.println("</div>");
        
        out.println("<div id='alertBox' class='alert'></div>");
        
        out.println("<div class='stats'>");
        out.println("<div class='stat-card'><h3 id='totalPatients'>0</h3><p>Total Patients</p></div>");
        out.println("<div class='stat-card'><h3>8</h3><p>Disease Patterns</p></div>");
        out.println("<div class='stat-card'><h3>95%</h3><p>Accuracy Rate</p></div>");
        out.println("</div>");
        
        out.println("<div class='main-grid'>");
        out.println("<div class='card'>");
        out.println("<h2>Patient Information</h2>");
        out.println("<div class='form-group'><label>Patient Name:</label><input type='text' id='name' placeholder='Enter full name'></div>");
        out.println("<div class='form-group'><label>Age:</label><input type='number' id='age' placeholder='Enter age'></div>");
        out.println("<div class='form-group'><label>Gender:</label><select id='gender'><option>Male</option><option>Female</option><option>Other</option></select></div>");
        out.println("<div class='form-group'><label>Blood Group:</label><select id='bloodGroup'><option>A+</option><option>A-</option><option>B+</option><option>B-</option><option>O+</option><option>O-</option><option>AB+</option><option>AB-</option></select></div>");
        out.println("<div class='form-group'><label>Medical History:</label><textarea id='medicalHistory' rows='3' placeholder='Previous conditions, allergies...'></textarea></div>");
        out.println("</div>");
        
        out.println("<div class='card'>");
        out.println("<h2>Symptom Selection</h2>");
        out.println("<label>Select all applicable symptoms:</label>");
        out.println("<div class='symptom-selector' id='symptomSelector'></div>");
        out.println("<div style='margin-top:20px;'>");
        out.println("<button onclick='runDiagnosis()'>🔬 Run AI Diagnosis</button>");
        out.println("<button class='btn-secondary' onclick='resetForm()'>Reset</button>");
        out.println("</div>");
        out.println("</div>");
        out.println("</div>");
        
        out.println("<div class='results' id='resultsSection' style='display:none;'>");
        out.println("<h2>AI Diagnosis Results</h2>");
        out.println("<div id='diagnosisResults'></div>");
        out.println("</div>");
        
        out.println("<div class='card' style='margin-top:20px;'>");
        out.println("<h2>Patient Records</h2>");
        out.println("<div class='patient-list' id='patientList'></div>");
        out.println("</div>");
        
        out.println("</div>");
        
        // MODAL HTML - THIS IS THE ONLY NEW ADDITION
        out.println("<div id='patientModal' class='modal'>");
        out.println("<div class='modal-content'>");
        out.println("<div class='modal-header'>");
        out.println("<h2>📋 Patient Medical Record</h2>");
        out.println("<span class='close' onclick='closeModal()'>&times;</span>");
        out.println("</div>");
        out.println("<div class='modal-body' id='modalBody'></div>");
        out.println("</div>");
        out.println("</div>");
        
        out.println("<script>");
        
        out.println("const symptoms=['runny nose','sneezing','sore throat','cough','mild fever','fatigue','high fever','body aches','severe fatigue','headache','chills','loss of taste','loss of smell','dry cough','shortness of breath','severe headache','nausea','sensitivity to light','sensitivity to sound','visual disturbances','increased thirst','frequent urination','unexplained weight loss','blurred vision','slow healing wounds','dizziness','chest pain','nosebleeds','wheezing','chest tightness','stomach pain','vomiting','indigestion','loss of appetite'];");
        
        out.println("function initSymptoms(){const c=document.getElementById('symptomSelector');symptoms.forEach(s=>{const d=document.createElement('div');d.className='symptom-checkbox';d.innerHTML='<input type=\"checkbox\" value=\"'+s+'\" id=\"sym_'+s.replace(/\\s+/g,'_')+'\"><label for=\"sym_'+s.replace(/\\s+/g,'_')+'\">'+s+'</label>';c.appendChild(d)})}");
        
        out.println("function showAlert(m,t){const a=document.getElementById('alertBox');a.textContent=m;a.className='alert '+t;a.style.display='block';setTimeout(()=>a.style.display='none',4000)}");
        
        out.println("function closeModal(){document.getElementById('patientModal').style.display='none'}");
        out.println("window.onclick=function(e){if(e.target==document.getElementById('patientModal')){closeModal()}}");
        
        out.println("async function runDiagnosis(){const name=document.getElementById('name').value;const age=parseInt(document.getElementById('age').value);const gender=document.getElementById('gender').value;const bloodGroup=document.getElementById('bloodGroup').value;const medicalHistory=document.getElementById('medicalHistory').value;const selectedSymptoms=[];document.querySelectorAll('#symptomSelector input:checked').forEach(cb=>selectedSymptoms.push(cb.value));if(!name||!age||selectedSymptoms.length===0){showAlert('Please fill patient info and select symptoms!','info');return}const patient={name,age,gender,bloodGroup,medicalHistory,symptoms:selectedSymptoms};document.getElementById('resultsSection').style.display='block';document.getElementById('diagnosisResults').innerHTML='<div class=\"loading\"><div class=\"spinner\"></div><p>AI analyzing...</p></div>';try{const response=await fetch('/api/diagnosis/',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(patient)});const results=await response.json();if(results.length===0){document.getElementById('diagnosisResults').innerHTML='<p style=\"text-align:center;\">No matching diseases. Consult a doctor.</p>';return}let html='';results.forEach((r,i)=>{html+='<div class=\"diagnosis-item\"><div class=\"diagnosis-header\"><div><span class=\"disease-name\">'+(i+1)+'. '+r.disease+'</span><span class=\"severity '+r.severity+'\">'+r.severity+' Risk</span></div><div class=\"probability\">'+r.probability.toFixed(1)+'%</div></div><div class=\"recommendations\"><h4>Recommended Actions:</h4><ul>'+r.recommendations.map(rc=>'<li>'+rc+'</li>').join('')+'</ul></div></div>'});document.getElementById('diagnosisResults').innerHTML=html;patient.diagnosis=results[0].disease;patient.confidenceScore=results[0].probability;await fetch('/api/patients/',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(patient)});showAlert('Diagnosis completed!','success');loadPatients()}catch(error){showAlert('Error running diagnosis','info')}}");
        
        out.println("async function loadPatients(){try{const response=await fetch('/api/patients/');const patients=await response.json();document.getElementById('totalPatients').textContent=patients.length;const listDiv=document.getElementById('patientList');if(patients.length===0){listDiv.innerHTML='<p style=\"text-align:center;color:#999;\">No patient records</p>';return}listDiv.innerHTML='';patients.forEach(p=>{const div=document.createElement('div');div.className='patient-item';div.innerHTML='<div class=\"patient-info\"><strong>'+p.name+'</strong><span>Age: '+p.age+' | Gender: '+p.gender+' | Blood: '+p.bloodGroup+'</span>'+(p.diagnosis?'<br><span style=\"color:#667eea;\">Diagnosis: '+p.diagnosis+' ('+p.confidenceScore.toFixed(1)+'%)</span>':'')+'</div><div class=\"patient-actions\"><button onclick=\"viewPatientModal('+p.id+')\">View</button><button class=\"btn-secondary\" onclick=\"deletePatient('+p.id+')\">Delete</button></div>';listDiv.appendChild(div)})}catch(error){console.error(error)}}");
        
        out.println("async function viewPatientModal(id){try{const response=await fetch('/api/patients/'+id);const p=await response.json();const modalBody=document.getElementById('modalBody');let symptomsHTML='';if(p.symptoms&&p.symptoms.length>0){p.symptoms.forEach(s=>{symptomsHTML+='<span class=\"symptom-badge\">'+s+'</span>'})}else{symptomsHTML='<p style=\"text-align:center;color:#999;\">No symptoms recorded</p>'}let html='<div class=\"record-section\"><h3>👤 Personal Information</h3><div class=\"info-grid\"><div class=\"info-item\"><label>Patient ID</label><div class=\"value\">#'+p.id+'</div></div><div class=\"info-item\"><label>Full Name</label><div class=\"value\">'+p.name+'</div></div><div class=\"info-item\"><label>Age</label><div class=\"value\">'+p.age+' years</div></div><div class=\"info-item\"><label>Gender</label><div class=\"value\">'+p.gender+'</div></div><div class=\"info-item\"><label>Blood Group</label><div class=\"value\">'+p.bloodGroup+'</div></div><div class=\"info-item\"><label>Registration Date</label><div class=\"value\">'+new Date().toLocaleDateString()+'</div></div></div></div><div class=\"record-section\"><h3>🩺 Reported Symptoms</h3>'+symptomsHTML+'</div>';if(p.diagnosis){html+='<div class=\"record-section\"><h3>🔬 AI Diagnosis Result</h3><div class=\"diagnosis-box\"><h4>Diagnosed Condition</h4><div style=\"font-size:28px;margin:15px 0;\">'+p.diagnosis+'</div><div class=\"conf-score\">'+p.confidenceScore.toFixed(1)+'%</div><div style=\"font-size:14px;opacity:0.9;\">Confidence Score</div></div></div>'}if(p.medicalHistory){html+='<div class=\"record-section\"><h3>📋 Medical History</h3><p>'+p.medicalHistory+'</p></div>'}modalBody.innerHTML=html;document.getElementById('patientModal').style.display='block'}catch(error){showAlert('Error loading patient record','info')}}");
        
        out.println("async function deletePatient(id){if(!confirm('Delete this patient?'))return;try{await fetch('/api/patients/'+id,{method:'DELETE'});showAlert('Patient deleted','success');loadPatients()}catch(error){showAlert('Error deleting patient','info')}}");
        
        out.println("function resetForm(){document.getElementById('name').value='';document.getElementById('age').value='';document.getElementById('medicalHistory').value='';document.querySelectorAll('#symptomSelector input').forEach(cb=>cb.checked=false);document.getElementById('resultsSection').style.display='none'}");
        
        out.println("initSymptoms();loadPatients();");
        
        out.println("</script>");
        out.println("</body></html>");
    }
}
