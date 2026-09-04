// TalentPulse — Enterprise UI Controller

// --- Utility Functions ---
async function apiFetch(path, options = {}) {
  const res = await fetch(path, options);
  if (!res.ok) {
    const txt = await res.text();
    let msg = txt;
    try {
      const parsed = JSON.parse(txt);
      if (parsed.message) msg = parsed.message;
    } catch (_) {}
    throw new Error(msg || res.statusText);
  }
  return res.json().catch(() => null);
}

function parseScore(val) {
  if (val === undefined || val === null || isNaN(val)) return 0;
  const num = Number(val);
  return Math.min(100, Math.max(0, Math.round(num > 1 ? num : num * 100)));
}

function showToast(message, type = 'info') {
  const container = document.getElementById('toastContainer');
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `<i class="fa-solid ${type === 'success' ? 'fa-circle-check' : type === 'error' ? 'fa-circle-xmark' : 'fa-circle-info'}"></i> ${message}`;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(20px)';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

// --- Navigation Tabs ---
document.querySelectorAll('.nav-tab').forEach(tabBtn => {
  tabBtn.addEventListener('click', () => {
    document.querySelectorAll('.nav-tab').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));

    tabBtn.classList.add('active');
    const targetId = tabBtn.getAttribute('data-tab');
    const targetContent = document.getElementById(targetId);
    if (targetContent) targetContent.classList.add('active');
  });
});

// --- File Upload & Drag-and-Drop Handling ---
const dropZone = document.getElementById('dropZone');
const resumeFileInput = document.getElementById('resumeFile');
const uploadBtn = document.getElementById('uploadBtn');
const selectedFileInfo = document.getElementById('selectedFileInfo');

if (dropZone && resumeFileInput) {
  dropZone.addEventListener('dragover', (e) => {
    e.preventDefault();
    dropZone.classList.add('dragover');
  });

  dropZone.addEventListener('dragleave', () => dropZone.classList.remove('dragover'));

  dropZone.addEventListener('drop', (e) => {
    e.preventDefault();
    dropZone.classList.remove('dragover');
    if (e.dataTransfer.files.length) {
      resumeFileInput.files = e.dataTransfer.files;
      handleFileSelected();
    }
  });

  resumeFileInput.addEventListener('change', handleFileSelected);
}

function handleFileSelected() {
  const file = resumeFileInput.files[0];
  if (file) {
    const sizeMb = (file.size / (1024 * 1024)).toFixed(2);
    selectedFileInfo.innerHTML = `<i class="fa-solid fa-file-lines"></i> ${file.name} (${sizeMb} MB)`;
    uploadBtn.disabled = false;
  } else {
    selectedFileInfo.innerHTML = '';
    uploadBtn.disabled = true;
  }
}

// --- Resume Parser Action ---
uploadBtn.addEventListener('click', async () => {
  const file = resumeFileInput.files[0];
  if (!file) return showToast('Please select a resume file first', 'error');

  const fd = new FormData();
  fd.append('file', file);

  const statusBadge = document.getElementById('parsingStatusBadge');
  const resultContainer = document.getElementById('uploadResultContainer');

  statusBadge.className = 'badge badge-primary';
  statusBadge.textContent = 'Parsing...';
  uploadBtn.disabled = true;
  uploadBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Extracting Information...';

  try {
    const data = await apiFetch('/api/resumes/upload', { method: 'POST', body: fd });
    statusBadge.className = 'badge badge-success';
    statusBadge.textContent = 'Success';
    
    renderCandidateProfile(data, resultContainer);
    showToast('Resume parsed successfully!', 'success');
    await refreshCandidates();
  } catch (e) {
    statusBadge.className = 'badge badge-neutral';
    statusBadge.textContent = 'Failed';
    resultContainer.innerHTML = `<div class="empty-state" style="color: var(--danger);"><i class="fa-solid fa-triangle-exclamation" style="font-size: 24px; margin-bottom: 8px;"></i><p>${e.message}</p></div>`;
    showToast('Failed to parse resume: ' + e.message, 'error');
  } finally {
    uploadBtn.disabled = false;
    uploadBtn.innerHTML = '<i class="fa-solid fa-wand-magic-sparkles"></i> Analyze & Extract Profile';
  }
});

function renderCandidateProfile(data, container) {
  if (!data) return;

  const skillsHtml = (data.skills || []).map(s => `<span class="tag tag-skill">${s}</span>`).join('') || '<span class="text-dim">No explicit skills detected</span>';
  const eduHtml = (data.education || []).map(e => `<div class="chip"><i class="fa-solid fa-graduation-cap"></i> ${e}</div>`).join('') || '';
  const expHtml = (data.experience || []).map(ex => `<div class="chip"><i class="fa-solid fa-briefcase"></i> ${ex}</div>`).join('') || '';

  const initial = (data.name ? data.name.charAt(0) : (data.email ? data.email.charAt(0) : 'C')).toUpperCase();

  container.innerHTML = `
    <div class="profile-card">
      <div class="profile-avatar-row">
        <div class="profile-avatar">${initial}</div>
        <div class="profile-title">
          <h4>${data.name || 'Anonymous Candidate'}</h4>
          <p>ID #${data.candidateId}</p>
        </div>
      </div>

      <div class="contact-chips">
        ${data.email ? `<div class="chip"><i class="fa-solid fa-envelope"></i> ${data.email}</div>` : ''}
        ${data.phone ? `<div class="chip"><i class="fa-solid fa-phone"></i> ${data.phone}</div>` : ''}
      </div>

      <div>
        <div class="profile-section-title">Extracted Skills (${(data.skills || []).length})</div>
        <div class="tag-list">${skillsHtml}</div>
      </div>

      ${data.experience && data.experience.length ? `
        <div>
          <div class="profile-section-title">Work Experience</div>
          <div class="contact-chips" style="margin-top: 6px;">${expHtml}</div>
        </div>
      ` : ''}

      ${data.education && data.education.length ? `
        <div>
          <div class="profile-section-title">Education History</div>
          <div class="contact-chips" style="margin-top: 6px;">${eduHtml}</div>
        </div>
      ` : ''}
    </div>
  `;
}

// --- Job Posting Action ---
document.getElementById('jobForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const title = document.getElementById('jobTitle').value.trim();
  const desc = document.getElementById('jobDesc').value.trim();
  const skillsStr = document.getElementById('jobSkills').value;
  const skills = skillsStr.split(',').map(s => s.trim()).filter(Boolean);
  const minExp = parseInt(document.getElementById('jobMinExp').value || '0', 10);

  try {
    const body = { title, description: desc, requiredSkills: skills, minimumExperience: minExp };
    const created = await apiFetch('/api/jobs', {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify(body)
    });
    
    showToast(`Job posting created for "${created.title}"`, 'success');
    document.getElementById('jobForm').reset();
    await loadJobs();
  } catch (err) {
    showToast('Failed to create job: ' + err.message, 'error');
  }
});

async function loadJobs() {
  try {
    const jobs = await apiFetch('/api/jobs') || [];
    const jobsList = document.getElementById('jobsList');
    const jobSelect = document.getElementById('jobSelect');
    const countBadge = document.getElementById('jobsCountBadge');
    const jobStat = document.getElementById('jobCountStat');

    countBadge.textContent = `${jobs.length} Jobs`;
    if (jobStat) jobStat.textContent = jobs.length;

    jobsList.innerHTML = '';
    jobSelect.innerHTML = '<option value="">Select a job position...</option>';

    if (!jobs.length) {
      jobsList.innerHTML = '<div class="empty-state">No job postings created yet.</div>';
      return;
    }

    jobs.forEach(j => {
      // List Item
      const item = document.createElement('div');
      item.className = 'job-item';
      const skillTags = (j.requiredSkills || []).map(s => `<span class="tag tag-skill">${s}</span>`).join('');
      
      item.innerHTML = `
        <div class="job-item-header">
          <span class="job-item-title">${j.title}</span>
          <div style="display: flex; align-items: center; gap: 8px;">
            <span class="job-item-id">#${j.id}</span>
            <button class="btn btn-outline" style="padding: 4px 8px; font-size: 11px; color: var(--danger); border-color: rgba(239,68,68,0.3);" onclick="deleteJobItem(${j.id})">
              <i class="fa-solid fa-trash"></i>
            </button>
          </div>
        </div>
        <div class="job-item-desc">${j.description || ''}</div>
        <div class="tag-list">${skillTags}</div>
      `;
      jobsList.appendChild(item);

      // Select Option
      const opt = document.createElement('option');
      opt.value = j.id;
      opt.textContent = `${j.title} (#${j.id})`;
      jobSelect.appendChild(opt);
    });
  } catch (e) {
    console.error('Failed to load jobs', e);
  }
}

async function deleteJobItem(id) {
  if (!confirm('Are you sure you want to delete this job position?')) return;
  try {
    await fetch(`/api/jobs/${id}`, { method: 'DELETE' });
    showToast('Job position deleted', 'info');
    await loadJobs();
  } catch (e) {
    showToast('Failed to delete job: ' + e.message, 'error');
  }
}

async function refreshCandidates() {
  try {
    const cands = await apiFetch('/api/candidates') || [];
    const select = document.getElementById('candidateSelect');
    const candStat = document.getElementById('candidateCountStat');
    if (candStat) candStat.textContent = cands.length;

    select.innerHTML = '<option value="">Select a candidate...</option>';

    cands.forEach(c => {
      const opt = document.createElement('option');
      opt.value = c.id;
      opt.textContent = `${c.name || c.email || ('Candidate #' + c.id)}`;
      select.appendChild(opt);
    });
  } catch (e) {
    console.error('Failed to load candidates', e);
  }
}

// --- Matching Actions ---
document.getElementById('matchBtn').addEventListener('click', async () => {
  const candidateId = document.getElementById('candidateSelect').value;
  const jobId = document.getElementById('jobSelect').value;
  
  if (!candidateId || !jobId) {
    return showToast('Please select both a candidate and a job requisition', 'error');
  }

  const resultContainer = document.getElementById('matchResult');
  resultContainer.innerHTML = '<div class="result-placeholder"><i class="fa-solid fa-spinner fa-spin"></i><p>Calculating skill vector match & experience compatibility...</p></div>';

  try {
    const resp = await apiFetch(`/api/jobs/${jobId}/match/${candidateId}`);
    renderMatchResult(resp, resultContainer);
  } catch (e) {
    resultContainer.innerHTML = `<div class="empty-state" style="color: var(--danger);"><p>${e.message}</p></div>`;
    showToast('Matching error: ' + e.message, 'error');
  }
});

function renderMatchResult(data, container) {
  if (!data) return;

  const pct = parseScore(data.overallScore);
  const skillPct = parseScore(data.skillMatchScore);
  const expPct = parseScore(data.experienceScore);

  const matchedTags = (data.matchedSkills || []).map(s => `<span class="tag tag-matched"><i class="fa-solid fa-check"></i> ${s}</span>`).join('') || '<span class="text-dim">None</span>';
  const missingTags = (data.missingSkills || []).map(s => `<span class="tag tag-missing"><i class="fa-solid fa-xmark"></i> ${s}</span>`).join('') || '<span class="text-dim">None</span>';

  container.innerHTML = `
    <div class="score-card">
      <div class="score-main">
        <div class="score-badge-circle" style="--score-pct: ${pct};">
          <div class="score-badge-inner">${pct}%</div>
        </div>
        <div class="score-meta">
          <h4>${data.jobTitle || 'Job Position'}</h4>
          <p>Skill Match: <strong>${skillPct}%</strong> | Exp Score: <strong>${expPct}%</strong></p>
          ${data.recommendation ? `<span class="badge badge-success" style="margin-top: 6px; display: inline-block;">${data.recommendation}</span>` : ''}
        </div>
      </div>

      <div>
        <div class="profile-section-title">Matched Skills (${(data.matchedSkills || []).length})</div>
        <div class="tag-list" style="margin-top: 6px;">${matchedTags}</div>
      </div>

      <div>
        <div class="profile-section-title">Missing Skills (${(data.missingSkills || []).length})</div>
        <div class="tag-list" style="margin-top: 6px;">${missingTags}</div>
      </div>
    </div>
  `;
}

// --- Top 5 Recommendations ---
document.getElementById('recommendBtn').addEventListener('click', async () => {
  const candidateId = document.getElementById('candidateSelect').value;
  if (!candidateId) return showToast('Please select a candidate to get recommendations', 'error');

  const ul = document.getElementById('recsList');
  ul.innerHTML = '<div class="result-placeholder"><i class="fa-solid fa-spinner fa-spin"></i><p>Finding top job recommendations...</p></div>';

  try {
    const recs = await apiFetch(`/api/candidates/${candidateId}/recommendations`) || [];
    ul.innerHTML = '';

    if (!recs.length) {
      ul.innerHTML = '<div class="empty-state">No matching recommendations found for this candidate.</div>';
      return;
    }

    recs.forEach(r => {
      const pct = parseScore(r.matchScore);
      const li = document.createElement('li');
      li.className = 'rec-item';
      li.innerHTML = `
        <div>
          <div class="rec-title">${r.title}</div>
          <div class="job-item-id">Job Position #${r.jobId}</div>
        </div>
        <div class="rec-score">${pct}% Match</div>
      `;
      ul.appendChild(li);
    });
  } catch (e) {
    ul.innerHTML = `<div class="empty-state" style="color: var(--danger);"><p>${e.message}</p></div>`;
  }
});

// --- Search Jobs Action ---
document.getElementById('searchBtn').addEventListener('click', runSearch);
document.getElementById('searchQuery').addEventListener('keypress', (e) => {
  if (e.key === 'Enter') runSearch();
});

async function runSearch() {
  const q = document.getElementById('searchQuery').value.trim();
  if (!q) return showToast('Enter a query or keyword to search', 'error');

  const container = document.getElementById('searchResults');
  container.innerHTML = '<div class="result-placeholder"><i class="fa-solid fa-spinner fa-spin"></i><p>Searching job database...</p></div>';

  try {
    const results = await apiFetch(`/api/jobs/search?query=${encodeURIComponent(q)}&topN=10`) || [];
    container.innerHTML = '';

    if (!results.length) {
      container.innerHTML = '<div class="empty-state">No positions matched your search query.</div>';
      return;
    }

    results.forEach(r => {
      const pct = parseScore(r.matchScore);
      const card = document.createElement('div');
      card.className = 'job-item';
      card.innerHTML = `
        <div class="job-item-header">
          <span class="job-item-title">${r.title}</span>
          <span class="badge badge-success">${pct}% Match</span>
        </div>
        <div class="job-item-id">Job ID #${r.jobId}</div>
      `;
      container.appendChild(card);
    });
  } catch (e) {
    container.innerHTML = `<div class="empty-state" style="color: var(--danger);"><p>${e.message}</p></div>`;
  }
}

// Initial Load
loadJobs();
refreshCandidates();
