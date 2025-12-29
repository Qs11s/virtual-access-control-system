const origin = window.location.origin;
const API_BASE = origin.replace("-4173", "-8080").replace(":4173", ":8080");

const TEACHER_REGISTER_SECRET = "teacher-register-2025";
const ADMIN_REGISTER_SECRET = "admin-register-2025";

let teacherRegisterUnlocked = false;
let adminRegisterUnlocked = false;

let currentUsername = null;
let currentRole = null;

function inferRoleFromUsername(username) {
  if (!username) return null;
  const u = username.toLowerCase();
  if (u.startsWith("admin")) return "ADMIN";
  if (u.startsWith("teacher")) return "TEACHER";
  return "STUDENT";
}

function isAdmin() {
  return currentRole === "ADMIN";
}

function isTeacher() {
  return currentRole === "TEACHER";
}

function isStudent() {
  return currentRole === "STUDENT";
}

async function apiFetch(path, options = {}) {
  const token = localStorage.getItem("token");
  const headers = { ...(options.headers || {}) };
  if (!(options.body instanceof FormData)) {
    headers["Content-Type"] = headers["Content-Type"] || "application/json";
  }
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }
  const res = await fetch(API_BASE + path, { ...options, headers });
  const text = await res.text();
  let data;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = text;
  }
  return { status: res.status, ok: res.ok, data };
}

function showMessage(el, text, type) {
  if (!el) return;
  el.textContent = typeof text === "string" ? text : "";
  if (!type) {
    el.className = "message";
  } else {
    el.className = "message message-" + type;
  }
}

const loginView = document.getElementById("login-view");
const dashboardView = document.getElementById("dashboard-view");

const loginForm = document.getElementById("login-form");
const loginMessageEl = document.getElementById("login-message");

const tabStudentRegister = document.getElementById("tab-student-register");
const tabTeacherRegister = document.getElementById("tab-teacher-register");
const tabAdminRegister = document.getElementById("tab-admin-register");

const studentRegisterSection = document.getElementById("student-register-section");
const teacherRegisterSection = document.getElementById("teacher-register-section");
const adminRegisterSection = document.getElementById("admin-register-section");

const studentRegisterForm = document.getElementById("register-form");
const studentRegisterMessageEl = document.getElementById("register-message");

const teacherRegisterForm = document.getElementById("teacher-register-form");
const teacherRegisterMessageEl = document.getElementById("teacher-register-message");

const adminRegisterForm = document.getElementById("admin-register-form");
const adminRegisterMessageEl = document.getElementById("admin-register-message");

const currentUsernameEl = document.getElementById("current-username");
const currentRoleTextEl = document.getElementById("current-role-text");
const logoutBtn = document.getElementById("logout-btn");

const checkinForm = document.getElementById("checkin-form");
const checkinMessageEl = document.getElementById("checkin-message");
const checkoutBtn = document.getElementById("checkout-btn");

const teacherSessionForm = document.getElementById("teacher-session-form");
const teacherSessionInput = document.getElementById("teacher-session-id");
const teacherSummaryEl = document.getElementById("teacher-summary");
const teacherAttendanceTbody = document.getElementById("teacher-attendance-tbody");

const teacherEarlyEndForm = document.getElementById("teacher-early-end-form");
const teacherEarlyEndMessageEl = document.getElementById("teacher-early-end-message");
const teacherEarlyEndSessionInput = document.getElementById("teacher-early-end-session-id");

const adminSessionForm = document.getElementById("admin-session-form");
const adminSessionInput = document.getElementById("admin-session-id");
const adminSummaryEl = document.getElementById("admin-summary");
const adminAttendanceTbody = document.getElementById("admin-attendance-tbody");

const adminTempCodeForm = document.getElementById("admin-tempcode-form");
const adminTempCodeMessageEl = document.getElementById("admin-tempcode-message");
const adminTempCodeResultEl = document.getElementById("admin-tempcode-result");

const tempCodeVerifyForm = document.getElementById("tempcode-verify-form");
const tempCodeVerifyMessageEl = document.getElementById("tempcode-verify-message");

const roleSections = document.querySelectorAll("[data-role-section]");

function showLoginView() {
  if (loginView) loginView.style.display = "";
  if (dashboardView) dashboardView.style.display = "none";
}

function showDashboardView() {
  if (loginView) loginView.style.display = "none";
  if (dashboardView) dashboardView.style.display = "";
}

function refreshRoleUI() {
  if (!currentUsername) {
    if (currentUsernameEl) currentUsernameEl.textContent = "";
    if (currentRoleTextEl) currentRoleTextEl.textContent = "";
    roleSections.forEach((sec) => {
      sec.style.display = "none";
    });
    return;
  }
  if (currentUsernameEl) currentUsernameEl.textContent = currentUsername;
  roleSections.forEach((sec) => {
    const role = sec.dataset.roleSection;
    if (!role) {
      sec.style.display = "";
    } else if (role === currentRole) {
      sec.style.display = "";
    } else {
      sec.style.display = "none";
    }
  });
  if (currentRoleTextEl) {
    if (isAdmin()) {
      currentRoleTextEl.textContent = "当前角色：管理员（ROLE_ADMIN）";
    } else if (isTeacher()) {
      currentRoleTextEl.textContent = "当前角色：教师（ROLE_TEACHER）";
    } else {
      currentRoleTextEl.textContent = "当前角色：学生（ROLE_STUDENT）";
    }
  }
}

function switchRegisterTab(tab) {
  if (!studentRegisterSection || !teacherRegisterSection || !adminRegisterSection) return;
  studentRegisterSection.style.display = "none";
  teacherRegisterSection.style.display = "none";
  adminRegisterSection.style.display = "none";
  if (tab === "student") {
    studentRegisterSection.style.display = "";
  } else if (tab === "teacher") {
    teacherRegisterSection.style.display = "";
  } else if (tab === "admin") {
    adminRegisterSection.style.display = "";
  }
}

if (tabStudentRegister) {
  tabStudentRegister.addEventListener("click", () => {
    switchRegisterTab("student");
  });
}

if (tabTeacherRegister) {
  tabTeacherRegister.addEventListener("click", () => {
    if (!teacherRegisterUnlocked) {
      const value = window.prompt("请输入教师注册口令：");
      if (value !== TEACHER_REGISTER_SECRET) {
        alert("口令错误，无法进入教师注册页面");
        return;
      }
      teacherRegisterUnlocked = true;
    }
    switchRegisterTab("teacher");
  });
}

if (tabAdminRegister) {
  tabAdminRegister.addEventListener("click", () => {
    if (!adminRegisterUnlocked) {
      const value = window.prompt("请输入管理员注册口令：");
      if (value !== ADMIN_REGISTER_SECRET) {
        alert("口令错误，无法进入管理员注册页面");
        return;
      }
      adminRegisterUnlocked = true;
    }
    switchRegisterTab("admin");
  });
}

switchRegisterTab("student");

if (loginForm) {
  loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    showMessage(loginMessageEl, "正在登录...", "info");
    const username = document.getElementById("login-username").value.trim();
    const password = document.getElementById("login-password").value;
    try {
      const res = await apiFetch("/auth/login", {
        method: "POST",
        body: JSON.stringify({ username, password })
      });
      if (!res.ok) {
        let msg = "登录失败，状态码：" + res.status;
        if (res.data && typeof res.data === "object" && res.data.message) {
          msg = res.data.message;
        }
        showMessage(loginMessageEl, msg, "error");
        return;
      }
      let token = null;
      if (res.data && typeof res.data === "object" && res.data.token) {
        token = res.data.token;
      } else if (typeof res.data === "string") {
        token = res.data;
      }
      if (!token) {
        showMessage(loginMessageEl, "登录失败：未获取到 token", "error");
        return;
      }
      localStorage.setItem("token", token);
      localStorage.setItem("username", username);
      currentUsername = username;
      currentRole = inferRoleFromUsername(username);
      showMessage(loginMessageEl, "登录成功！", "success");
      refreshRoleUI();
      showDashboardView();
    } catch (err) {
      console.error(err);
      showMessage(loginMessageEl, "登录请求出错", "error");
    }
  });
}

if (studentRegisterForm) {
  studentRegisterForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const username = document.getElementById("register-username").value.trim();
    const password = document.getElementById("register-password").value;
    if (!username || !password) {
      showMessage(studentRegisterMessageEl, "请输入用户名和密码", "error");
      return;
    }
    const lower = username.toLowerCase();
    if (lower.startsWith("admin") || lower.startsWith("teacher")) {
      showMessage(
        studentRegisterMessageEl,
        "学生用户名不能以 admin 或 teacher 开头，请改用教师/管理员注册入口",
        "error"
      );
      return;
    }
    showMessage(studentRegisterMessageEl, "正在注册学生账号...", "info");
    try {
      const res = await apiFetch("/auth/register", {
        method: "POST",
        body: JSON.stringify({ username, password })
      });
      if (!res.ok) {
        let msg = "注册失败，状态码：" + res.status;
        if (typeof res.data === "string") {
          msg = res.data;
        }
        showMessage(studentRegisterMessageEl, msg, "error");
        return;
      }
      if (typeof res.data === "string" && res.data.startsWith("Username already exists")) {
        showMessage(studentRegisterMessageEl, res.data, "error");
        return;
      }
      showMessage(studentRegisterMessageEl, "学生注册成功，可以用该账号登录", "success");
    } catch (err) {
      console.error(err);
      showMessage(studentRegisterMessageEl, "注册请求出错", "error");
    }
  });
}


if (teacherRegisterForm) {
  teacherRegisterForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const username = document.getElementById("teacher-register-username").value.trim();
    const password = document.getElementById("teacher-register-password").value;
    if (!username || !password) {
      showMessage(teacherRegisterMessageEl, "请输入用户名和密码", "error");
      return;
    }
    if (!username.toLowerCase().startsWith("teacher")) {
      showMessage(teacherRegisterMessageEl, "教师用户名必须以 teacher 开头，例如 teacher_zhang", "error");
      return;
    }
    showMessage(teacherRegisterMessageEl, "正在注册教师账号...", "info");
    try {
      const res = await apiFetch("/auth/register", {
        method: "POST",
        body: JSON.stringify({ username, password })
      });
      if (!res.ok) {
        let msg = "注册失败，状态码：" + res.status;
        if (typeof res.data === "string") {
          msg = res.data;
        }
        showMessage(teacherRegisterMessageEl, msg, "error");
        return;
      }
      if (typeof res.data === "string" && res.data.startsWith("Username already exists")) {
        showMessage(teacherRegisterMessageEl, res.data, "error");
        return;
      }
      showMessage(teacherRegisterMessageEl, "教师注册成功，可以用该账号登录", "success");
    } catch (err) {
      console.error(err);
      showMessage(teacherRegisterMessageEl, "注册请求出错", "error");
    }
  });
}

if (adminRegisterForm) {
  adminRegisterForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const username = document.getElementById("admin-register-username").value.trim();
    const password = document.getElementById("admin-register-password").value;
    if (!username || !password) {
      showMessage(adminRegisterMessageEl, "请输入用户名和密码", "error");
      return;
    }
    if (!username.toLowerCase().startsWith("admin")) {
      showMessage(adminRegisterMessageEl, "管理员用户名必须以 admin 开头，例如 admin_office", "error");
      return;
    }
    showMessage(adminRegisterMessageEl, "正在注册管理员账号...", "info");
    try {
      const res = await apiFetch("/auth/register", {
        method: "POST",
        body: JSON.stringify({ username, password })
      });
      if (!res.ok) {
        let msg = "注册失败，状态码：" + res.status;
        if (typeof res.data === "string") {
          msg = res.data;
        }
        showMessage(adminRegisterMessageEl, msg, "error");
        return;
      }
      if (typeof res.data === "string" && res.data.startsWith("Username already exists")) {
        showMessage(adminRegisterMessageEl, res.data, "error");
        return;
      }
      showMessage(adminRegisterMessageEl, "管理员注册成功，可以用该账号登录", "success");
    } catch (err) {
      console.error(err);
      showMessage(adminRegisterMessageEl, "注册请求出错", "error");
    }
  });
}

if (logoutBtn) {
  logoutBtn.addEventListener("click", () => {
    localStorage.removeItem("token");
    localStorage.removeItem("username");
    currentUsername = null;
    currentRole = null;

    const lu = document.getElementById("login-username");
    const lp = document.getElementById("login-password");
    const su = document.getElementById("register-username");
    const sp = document.getElementById("register-password");
    const tu = document.getElementById("teacher-register-username");
    const tp = document.getElementById("teacher-register-password");
    const au = document.getElementById("admin-register-username");
    const ap = document.getElementById("admin-register-password");

    if (lu) lu.value = "";
    if (lp) lp.value = "";
    if (su) su.value = "";
    if (sp) sp.value = "";
    if (tu) tu.value = "";
    if (tp) tp.value = "";
    if (au) au.value = "";
    if (ap) ap.value = "";

    showMessage(loginMessageEl, "", null);
    showMessage(studentRegisterMessageEl, "", null);
    showMessage(teacherRegisterMessageEl, "", null);
    showMessage(adminRegisterMessageEl, "", null);

    refreshRoleUI();
    showLoginView();
    switchRegisterTab("student");
  });
}

if (checkinForm) {
  checkinForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const sessionId = document.getElementById("session-id").value;
    if (!sessionId) {
      showMessage(checkinMessageEl, "请先输入 Session ID", "error");
      return;
    }
    showMessage(checkinMessageEl, "正在提交签到...", "info");
    try {
      const res = await apiFetch("/attendance/checkin", {
        method: "POST",
        body: JSON.stringify({ sessionId: Number(sessionId) })
      });
      if (res.ok) {
        let msg = "签到成功";
        if (res.data && typeof res.data === "object" && res.data.message) {
          msg = res.data.message;
        }
        showMessage(checkinMessageEl, msg, "success");
      } else {
        const msg = res.data && typeof res.data === "object" && res.data.message ? res.data.message : "签到失败，状态码：" + res.status;
        showMessage(checkinMessageEl, msg, "error");
      }
    } catch (err) {
      console.error(err);
      showMessage(checkinMessageEl, "签到请求出错", "error");
    }
  });
}

if (checkoutBtn) {
  checkoutBtn.addEventListener("click", async () => {
    const sessionId = document.getElementById("session-id").value;
    if (!sessionId) {
      showMessage(checkinMessageEl, "请先输入 Session ID", "error");
      return;
    }
    showMessage(checkinMessageEl, "正在提交签退...", "info");
    try {
      const res = await apiFetch("/attendance/checkout", {
        method: "POST",
        body: JSON.stringify({ sessionId: Number(sessionId) })
      });
      if (res.ok) {
        let msg = "签退成功";
        if (res.data && typeof res.data === "object" && res.data.message) {
          msg = res.data.message;
        }
        showMessage(checkinMessageEl, msg, "success");
      } else {
        const msg = res.data && typeof res.data === "object" && res.data.message ? res.data.message : "签退失败，状态码：" + res.status;
        showMessage(checkinMessageEl, msg, "error");
      }
    } catch (err) {
      console.error(err);
      showMessage(checkinMessageEl, "签退请求出错", "error");
    }
  });
}

async function loadTeacherAttendance(sessionId) {
  if (!teacherAttendanceTbody || !teacherSummaryEl) return;
  teacherAttendanceTbody.innerHTML = "";
  showMessage(teacherSummaryEl, "正在加载本节课考勤...", "info");
  try {
    const res = await apiFetch("/teacher/attendance/session/" + sessionId, {
      method: "GET"
    });
    if (!res.ok) {
      const msg = res.data && typeof res.data === "object" && res.data.message ? res.data.message : "加载失败，状态码：" + res.status;
      showMessage(teacherSummaryEl, msg, "error");
      return;
    }
    const list = Array.isArray(res.data) ? res.data : [];
    if (list.length === 0) {
      showMessage(teacherSummaryEl, "暂无考勤记录", "info");
    } else {
      showMessage(teacherSummaryEl, "共 " + list.length + " 条考勤记录", "info");
    }
    list.forEach((a) => {
      const tr = document.createElement("tr");

      const tdStuId = document.createElement("td");
      tdStuId.textContent = a.studentId;
      tr.appendChild(tdStuId);

      const tdStuName = document.createElement("td");
      tdStuName.textContent = a.studentUsername || "";
      tr.appendChild(tdStuName);

      const tdCheckIn = document.createElement("td");
      tdCheckIn.textContent = a.checkInTime ? a.checkInTime.replace("T", " ") : "";
      tr.appendChild(tdCheckIn);

      const tdCheckOut = document.createElement("td");
      tdCheckOut.textContent = a.checkOutTime ? a.checkOutTime.replace("T", " ") : "";
      tr.appendChild(tdCheckOut);

      const tdStatus = document.createElement("td");
      tdStatus.textContent = a.status || "";
      tr.appendChild(tdStatus);

      const tdApproved = document.createElement("td");
      if (typeof a.earlyLeaveApproved === "boolean") {
        tdApproved.textContent = a.earlyLeaveApproved ? "是" : "否";
      } else {
        tdApproved.textContent = "";
      }
      tr.appendChild(tdApproved);

      const tdReason = document.createElement("td");
      tdReason.textContent = a.earlyLeaveReason || "";
      tr.appendChild(tdReason);

      teacherAttendanceTbody.appendChild(tr);
    });
  } catch (err) {
    console.error(err);
    showMessage(teacherSummaryEl, "加载考勤时出错", "error");
  }
}

if (teacherSessionForm && teacherSessionInput) {
  teacherSessionForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    if (!isTeacher()) {
      showMessage(teacherSummaryEl, "仅教师可以查看课程考勤", "error");
      return;
    }
    const sessionId = teacherSessionInput.value;
    if (!sessionId) {
      showMessage(teacherSummaryEl, "请先输入 Session ID", "error");
      return;
    }
    await loadTeacherAttendance(Number(sessionId));
  });
}

if (teacherEarlyEndForm && teacherEarlyEndSessionInput) {
  teacherEarlyEndForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    if (!isTeacher()) {
      showMessage(teacherEarlyEndMessageEl, "仅教师可以提前下课", "error");
      return;
    }
    const sessionId = teacherEarlyEndSessionInput.value;
    if (!sessionId) {
      showMessage(teacherEarlyEndMessageEl, "请先输入 Session ID", "error");
      return;
    }
    if (!window.confirm("确认将本节课设置为提前结束？")) return;
    showMessage(teacherEarlyEndMessageEl, "正在提交提前下课请求...", "info");
    try {
      const res = await apiFetch("/teacher/attendance/session/" + Number(sessionId) + "/early-end", {
        method: "POST"
      });
      if (!res.ok) {
        const msg = res.data && typeof res.data === "object" && res.data.message ? res.data.message : "提前下课失败，状态码：" + res.status;
        showMessage(teacherEarlyEndMessageEl, msg, "error");
        return;
      }
      showMessage(teacherEarlyEndMessageEl, "已标记本节课提前结束", "success");
    } catch (err) {
      console.error(err);
      showMessage(teacherEarlyEndMessageEl, "提前下课请求出错", "error");
    }
  });
}

async function loadAdminAttendance(sessionId) {
  if (!adminAttendanceTbody || !adminSummaryEl) return;
  adminAttendanceTbody.innerHTML = "";
  showMessage(adminSummaryEl, "正在加载课程考勤...", "info");
  try {
    const res = await apiFetch("/admin/attendance/session/" + sessionId, {
      method: "GET"
    });
    if (!res.ok) {
      const msg = res.data && typeof res.data === "object" && res.data.message ? res.data.message : "加载失败，状态码：" + res.status;
      showMessage(adminSummaryEl, msg, "error");
      return;
    }
    const list = Array.isArray(res.data) ? res.data : [];
    if (list.length === 0) {
      showMessage(adminSummaryEl, "暂无考勤记录", "info");
    } else {
      showMessage(adminSummaryEl, "共 " + list.length + " 条考勤记录", "info");
    }
    list.forEach((a) => {
      const tr = document.createElement("tr");

      const tdStuId = document.createElement("td");
      tdStuId.textContent = a.studentId;
      tr.appendChild(tdStuId);

      const tdStuName = document.createElement("td");
      tdStuName.textContent = a.studentUsername || "";
      tr.appendChild(tdStuName);

      const tdCheckIn = document.createElement("td");
      tdCheckIn.textContent = a.checkInTime ? a.checkInTime.replace("T", " ") : "";
      tr.appendChild(tdCheckIn);

      const tdCheckOut = document.createElement("td");
      tdCheckOut.textContent = a.checkOutTime ? a.checkOutTime.replace("T", " ") : "";
      tr.appendChild(tdCheckOut);

      const tdStatus = document.createElement("td");
      tdStatus.textContent = a.status || "";
      tr.appendChild(tdStatus);

      const tdApproved = document.createElement("td");
      if (typeof a.earlyLeaveApproved === "boolean") {
        tdApproved.textContent = a.earlyLeaveApproved ? "是" : "否";
      } else {
        tdApproved.textContent = "";
      }
      tr.appendChild(tdApproved);

      const tdReason = document.createElement("td");
      tdReason.textContent = a.earlyLeaveReason || "";
      tr.appendChild(tdReason);

      adminAttendanceTbody.appendChild(tr);
    });
  } catch (err) {
    console.error(err);
    showMessage(adminSummaryEl, "加载考勤时出错", "error");
  }
}

if (adminSessionForm && adminSessionInput) {
  adminSessionForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    if (!isAdmin()) {
      showMessage(adminSummaryEl, "仅管理员可以查看考勤报表", "error");
      return;
    }
    const sessionId = adminSessionInput.value;
    if (!sessionId) {
      showMessage(adminSummaryEl, "请先输入 Session ID", "error");
      return;
    }
    await loadAdminAttendance(Number(sessionId));
  });
}

if (adminTempCodeForm) {
  adminTempCodeForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    if (!isAdmin()) {
      showMessage(adminTempCodeMessageEl, "仅管理员可以生成临时码", "error");
      return;
    }
    const locationId = Number(document.getElementById("tempcode-location-id").value);
    const minutes = Number(document.getElementById("tempcode-expires-minutes").value);
    const maxUses = Number(document.getElementById("tempcode-max-uses").value);
    if (!locationId || !minutes || !maxUses) {
      showMessage(adminTempCodeMessageEl, "请完整填写参数", "error");
      return;
    }
    showMessage(adminTempCodeMessageEl, "正在生成临时码...", "info");
    if (adminTempCodeResultEl) adminTempCodeResultEl.textContent = "";
    try {
      const res = await apiFetch("/access/temp-code", {
        method: "POST",
        body: JSON.stringify({
          locationId,
          expiresInMinutes: minutes,
          maxUses
        })
      });
      if (!res.ok) {
        const msg = res.data && typeof res.data === "object" && res.data.message ? res.data.message : "生成失败，状态码：" + res.status;
        showMessage(adminTempCodeMessageEl, msg, "error");
        return;
      }
      const code = res.data && res.data.code;
      const expiresAt = res.data && res.data.expiresAt;
      showMessage(adminTempCodeMessageEl, "临时码生成成功", "success");
      if (adminTempCodeResultEl) {
        adminTempCodeResultEl.textContent = "临时码：" + code + "，过期时间：" + expiresAt;
      }
    } catch (err) {
      console.error(err);
      showMessage(adminTempCodeMessageEl, "生成临时码请求出错", "error");
    }
  });
}

if (tempCodeVerifyForm) {
  tempCodeVerifyForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const locationId = Number(document.getElementById("verify-location-id").value);
    const code = document.getElementById("verify-code").value.trim();
    if (!locationId || !code) {
      showMessage(tempCodeVerifyMessageEl, "请输入 Location ID 和 临时码", "error");
      return;
    }
    showMessage(tempCodeVerifyMessageEl, "正在验证临时码...", "info");
    try {
      const res = await fetch(API_BASE + "/access/temp-code/verify", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ locationId, code })
      });
      const text = await res.text();
      let data;
      try {
        data = text ? JSON.parse(text) : null;
      } catch {
        data = text;
      }
      if (!res.ok) {
        const msg = data && typeof data === "object" && data.message ? data.message : "验证失败，状态码：" + res.status;
        showMessage(tempCodeVerifyMessageEl, msg, "error");
        return;
      }
      if (data && typeof data === "object") {
        if (data.result === "allow") {
          showMessage(tempCodeVerifyMessageEl, data.reason || "验证成功，允许开门", "success");
        } else {
          showMessage(tempCodeVerifyMessageEl, data.reason || "验证失败，禁止开门", "error");
        }
      } else {
        showMessage(tempCodeVerifyMessageEl, "收到未知响应", "error");
      }
    } catch (err) {
      console.error(err);
      showMessage(tempCodeVerifyMessageEl, "临时码验证请求出错", "error");
    }
  });
}

function init() {
  const savedToken = localStorage.getItem("token");
  const savedUsername = localStorage.getItem("username");
  if (savedToken && savedUsername) {
    currentUsername = savedUsername;
    currentRole = inferRoleFromUsername(savedUsername);
    refreshRoleUI();
    showDashboardView();
  } else {
    showLoginView();
    switchRegisterTab("student");
  }
}

init();
