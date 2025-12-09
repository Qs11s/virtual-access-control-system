const origin = window.location.origin;
const API_BASE = origin.replace("-4173", "-8080");

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

function showMessage(el, text, type = "info") {
  el.textContent = text || "";
  el.className = "message " + (type ? `message-${type}` : "");
}

const loginView = document.getElementById("login-view");
const dashboardView = document.getElementById("dashboard-view");

const loginForm = document.getElementById("login-form");
const loginMessageEl = document.getElementById("login-message");

const registerForm = document.getElementById("register-form");
const registerMessageEl = document.getElementById("register-message");

const currentUsernameEl = document.getElementById("current-username");
const currentRoleTextEl = document.getElementById("current-role-text");
const logoutBtn = document.getElementById("logout-btn");

const refreshCoursesBtn = document.getElementById("refresh-courses-btn");
const coursesTbody = document.getElementById("courses-tbody");
const coursesMessageEl = document.getElementById("courses-message");
const coursesActionsHeader = document.getElementById("courses-actions-header");

const createCourseForm = document.getElementById("create-course-form");
const createCourseMessageEl = document.getElementById("create-course-message");
const adminCreateCourseSection = document.getElementById("admin-create-course");

const checkinForm = document.getElementById("checkin-form");
const checkinMessageEl = document.getElementById("checkin-message");

const accessForm = document.getElementById("access-form");
const accessMessageEl = document.getElementById("access-message");

const studentCheckinSection = document.getElementById("student-checkin-section");

let currentUsername = null;

function isAdmin() {
  return currentUsername === "admin3";
}

function showLoginView() {
  loginView.style.display = "";
  dashboardView.style.display = "none";
}

function showDashboardView() {
  loginView.style.display = "none";
  dashboardView.style.display = "";
}

function refreshRoleUI() {
  if (!currentUsername) return;
  currentUsernameEl.textContent = currentUsername;
  if (isAdmin()) {
    currentRoleTextEl.textContent = "当前角色：管理员（ROLE_ADMIN）";
    adminCreateCourseSection.style.display = "";
    coursesActionsHeader.style.display = "";
  } else {
    currentRoleTextEl.textContent = "当前角色：学生（ROLE_STUDENT）";
    adminCreateCourseSection.style.display = "none";
    coursesActionsHeader.style.display = "none";
  }
}

loginForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  showMessage(loginMessageEl, "正在登录...", "info");
  const username = document.getElementById("login-username").value.trim();
  const password = document.getElementById("login-password").value;
  try {
    const res = await apiFetch("/auth/login", {
      method: "POST",
      body: JSON.stringify({ username, password }),
    });
    if (!res.ok) {
      showMessage(
        loginMessageEl,
        typeof res.data === "string" ? res.data : "登录失败",
        "error"
      );
      return;
    }
    const token = typeof res.data === "string" ? res.data : "";
    localStorage.setItem("token", token);
    localStorage.setItem("username", username);
    currentUsername = username;
    showMessage(loginMessageEl, "登录成功！", "success");
    refreshRoleUI();
    showDashboardView();
    await loadCourses();
  } catch (err) {
    console.error(err);
    showMessage(loginMessageEl, "登录请求出错", "error");
  }
});

registerForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  showMessage(registerMessageEl, "正在注册...", "info");
  const username = document.getElementById("register-username").value.trim();
  const password = document.getElementById("register-password").value;
  try {
    const res = await apiFetch("/auth/register", {
      method: "POST",
      body: JSON.stringify({ username, password }),
    });
    if (!res.ok) {
      showMessage(
        registerMessageEl,
        typeof res.data === "string" ? res.data : "注册失败",
        "error"
      );
      return;
    }
    showMessage(registerMessageEl, "注册成功，可以用此账号登录", "success");
  } catch (err) {
    console.error(err);
    showMessage(registerMessageEl, "注册请求出错", "error");
  }
});

logoutBtn.addEventListener("click", () => {
  localStorage.removeItem("token");
  localStorage.removeItem("username");
  currentUsername = null;
  showLoginView();
});

async function loadCourses() {
  showMessage(coursesMessageEl, "正在加载课程...", "info");
  coursesTbody.innerHTML = "";
  try {
    const res = await apiFetch("/courses", { method: "GET" });
    if (!res.ok) {
      showMessage(
        coursesMessageEl,
        `加载课程失败，状态码：${res.status}`,
        "error"
      );
      return;
    }
    const courses = Array.isArray(res.data) ? res.data : [];
    if (courses.length === 0) {
      showMessage(coursesMessageEl, "暂无课程", "info");
    } else {
      showMessage(coursesMessageEl, "", "info");
    }
    courses.forEach((c) => {
      const tr = document.createElement("tr");
      const tdId = document.createElement("td");
      tdId.textContent = c.id;
      tr.appendChild(tdId);
      const tdName = document.createElement("td");
      tdName.textContent = c.name || "";
      tr.appendChild(tdName);
      const tdCode = document.createElement("td");
      tdCode.textContent = c.code || "";
      tr.appendChild(tdCode);
      const tdTeacher = document.createElement("td");
      tdTeacher.textContent = c.teacher || "";
      tr.appendChild(tdTeacher);
      const tdDesc = document.createElement("td");
      tdDesc.textContent = c.description || "";
      tr.appendChild(tdDesc);
      if (isAdmin()) {
        const tdActions = document.createElement("td");
        const delBtn = document.createElement("button");
        delBtn.textContent = "删除";
        delBtn.className = "danger";
        delBtn.addEventListener("click", async () => {
          if (!confirm(`确定要删除课程 #${c.id} 吗？`)) return;
          try {
            const delRes = await apiFetch(`/courses/${c.id}`, {
              method: "DELETE",
            });
            if (!delRes.ok) {
              alert(`删除失败，状态码：${delRes.status}`);
            } else {
              alert("删除成功");
              await loadCourses();
            }
          } catch (err) {
            console.error(err);
            alert("删除请求出错");
          }
        });
        tdActions.appendChild(delBtn);
        tr.appendChild(tdActions);
      } else {
        const tdActions = document.createElement("td");
        tdActions.style.display = "none";
        tr.appendChild(tdActions);
      }
      coursesTbody.appendChild(tr);
    });
  } catch (err) {
    console.error(err);
    showMessage(coursesMessageEl, "加载课程时发生错误", "error");
  }
}

refreshCoursesBtn.addEventListener("click", loadCourses);

createCourseForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  if (!isAdmin()) {
    showMessage(createCourseMessageEl, "只有管理员可以创建课程", "error");
    return;
  }
  const name = document.getElementById("course-name").value.trim();
  const code = document.getElementById("course-code").value.trim();
  const description = document
    .getElementById("course-description")
    .value.trim();
  showMessage(createCourseMessageEl, "正在创建课程...", "info");
  try {
    const res = await apiFetch("/courses", {
      method: "POST",
      body: JSON.stringify({
        name,
        code: code || null,
        description: description || null,
      }),
    });
    if (!res.ok) {
      showMessage(
        createCourseMessageEl,
        `创建失败，状态码：${res.status}`,
        "error"
      );
      return;
    }
    showMessage(createCourseMessageEl, "课程创建成功", "success");
    createCourseForm.reset();
    await loadCourses();
  } catch (err) {
    console.error(err);
    showMessage(createCourseMessageEl, "创建课程请求出错", "error");
  }
});

checkinForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  const sessionId = document.getElementById("session-id").value;
  if (!sessionId) return;
  showMessage(checkinMessageEl, "正在提交签到...", "info");
  try {
    const res = await apiFetch(`/attendance/checkin?sessionId=${sessionId}`, {
      method: "POST",
    });
    if (res.ok) {
      if (res.data && typeof res.data === "object" && res.data.message) {
        showMessage(checkinMessageEl, res.data.message, "success");
      } else {
        showMessage(checkinMessageEl, "签到成功", "success");
      }
    } else if (res.status === 403 || res.status === 404) {
      const msg =
        res.data && typeof res.data === "object" && res.data.message
          ? res.data.message
          : `签到失败，状态码：${res.status}`;
      showMessage(checkinMessageEl, msg, "error");
    } else {
      showMessage(
        checkinMessageEl,
        `签到失败，状态码：${res.status}`,
        "error"
      );
    }
  } catch (err) {
    console.error(err);
    showMessage(checkinMessageEl, "签到请求出错", "error");
  }
});

accessForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  const locationId = document.getElementById("location-id").value;
  const qrToken = document.getElementById("qr-token").value.trim();
  if (!locationId || !qrToken) return;
  showMessage(accessMessageEl, "正在验证门禁...", "info");
  try {
    const payload = {
      locationId: Number(locationId),
      method: "QR",
      qrToken,
    };
    const res = await apiFetch("/access/scan", {
      method: "POST",
      body: JSON.stringify(payload),
    });
    if (!res.ok) {
      const msg =
        res.data && typeof res.data === "object" && res.data.message
          ? res.data.message
          : `请求失败，状态码：${res.status}`;
      showMessage(accessMessageEl, msg, "error");
      return;
    }
    if (res.data && typeof res.data === "object") {
      const allowed = res.data.allowed;
      const message = res.data.message;
      if (allowed) {
        showMessage(accessMessageEl, message || "开门成功", "success");
      } else {
        showMessage(accessMessageEl, message || "访问被拒绝", "error");
      }
    } else {
      showMessage(accessMessageEl, "收到未知响应", "error");
    }
  } catch (err) {
    console.error(err);
    showMessage(accessMessageEl, "门禁请求出错", "error");
  }
});

function init() {
  const savedToken = localStorage.getItem("token");
  const savedUsername = localStorage.getItem("username");
  if (savedToken && savedUsername) {
    currentUsername = savedUsername;
    refreshRoleUI();
    showDashboardView();
    loadCourses();
  } else {
    showLoginView();
  }
}

init();
