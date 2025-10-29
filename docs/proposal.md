# Project Proposal
**Project Title:** Virtual Access Control & Attendance Management System  
**Group No.:** TBC  
**Project Website URL:** (GitHub Repo URL)  
**Team Name:** SoftAccess

## Team Profile
- A — Backend (API & Authentication)
- B — Frontend (HTML/JS Interface)
- C — Data & Testing (MySQL Schema & Validation)
- D — Documentation & Coordination (Proposal/URS/UML)

## 1. Problem Diagnosis
Many clubs and labs still rely on paper sign-in or separate access and attendance systems.  
This results in:
- Time-consuming manual statistics
- Attendance errors / loopholes
- No role-based access control

## 2. Proposed Solution
Develop a **pure software virtual door system**:
- Users “check-in” via QR code / Passcode (simulates door entry)
- System records timestamps and calculates attendance automatically
- Admin manages access rules and exports reports in one click

**Key Benefit:** Reduce manual counting and improve fairness & efficiency.

## 3. Core Functional Features
- Virtual access event logging (QR/OTP)
- Door–User–Time permission control
- Shift / holiday configuration
- Automatic daily/weekly/monthly attendance summary
- One-click report export (CSV/XLSX)
- Audit history for administrator operations

## 4. Plan of Work & Responsibility
| Week(s) | Work Focus | Owner |
|--------|------------|-------|
| W1–2 | Requirement + Use-Case + Data Model | D + All |
| W3–4 | Virtual Entry & Event Logging (MVP) | A |
| W5–6 | Permission Policies & Shift Logic | B + C |
| W7–8 | Report UI & Export | B + D |
| W9 | Audit + Security + Testing | C |
| W10 | Demo + Final Docs | All |

## 5. Success Metrics
- Reduce attendance counting time ≥ **80%**
- Report accuracy ≥ **99%**
- Export attendance report ≤ **2 clicks**
- Daily attendance summary generated **automatically**

## 6. Resources Needed
- Frontend: HTML + JavaScript
- Backend: Lightweight API (to be added)
- Database: MySQL
- Diagram Tools: Draw.io / PlantUML
