/**
 * Student Management System — Frontend Client
 *
 * Communicates with the Spring Boot REST API at /api/students.
 * Pure Vanilla JavaScript — no frameworks.
 */

'use strict';

const API = '/api/students';

// ─── State ────────────────────────────────────────────────────────────────────
let allStudents   = [];   // full list received from server
let filteredList  = [];   // after search filtering
let pendingDelete = null; // { id, name } awaiting confirmation
let isSubmitting  = false;

// ─── DOM References ────────────────────────────────────────────────────────────
const $ = id => document.getElementById(id);

// Stats
const statTotal   = $('stat-total');
const statCourses = $('stat-courses');
const statAvgAge  = $('stat-avg-age');

// List & Search
const searchInput      = $('search-input');
const refreshBtn       = $('refresh-btn');
const tableBody        = $('table-body');
const mobileCards      = $('mobile-cards');
const stateContainer   = $('state-container');

// Add/Edit Student Modal
const formModal        = $('student-form-modal');
const formModalTitle   = $('form-modal-title');
const formCloseBtn     = $('form-modal-close-btn');
const formCancelBtn    = $('form-cancel-btn');
const formSubmitBtn    = $('form-submit-btn');
const submitLabel      = $('submit-label');
const studentForm      = $('student-form');

// Form Fields
const fieldId     = $('field-id');
const fieldName   = $('field-name');
const fieldEmail  = $('field-email');
const fieldCourse = $('field-course');
const fieldAge    = $('field-age');

// Field Errors
const errName   = $('err-name');
const errEmail  = $('err-email');
const errCourse = $('err-course');
const errAge    = $('err-age');

// Delete Confirm Modal
const deleteModal       = $('delete-modal');
const deleteStudentName = $('delete-student-name');
const deleteCancelBtn   = $('delete-cancel-btn');
const deleteConfirmBtn  = $('delete-confirm-btn');

// Toast container
const toastContainer = $('toast-container');

// ─── Bootstrap ────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    loadStudents();
    bindEvents();
});

function bindEvents() {
    // Page-level
    $('open-add-modal-btn').addEventListener('click', openAddModal);
    refreshBtn.addEventListener('click', () => loadStudents(true));
    searchInput.addEventListener('input', applySearch);

    // Add/Edit form modal
    formCloseBtn.addEventListener('click',  closeFormModal);
    formCancelBtn.addEventListener('click', closeFormModal);
    formSubmitBtn.addEventListener('click', handleFormSubmit);

    // Live field validation
    fieldName.addEventListener('input',  () => validateField('name'));
    fieldEmail.addEventListener('input', () => validateField('email'));
    fieldCourse.addEventListener('input',() => validateField('course'));
    fieldAge.addEventListener('input',   () => validateField('age'));

    // Delete confirm modal
    deleteCancelBtn.addEventListener('click',  closeDeleteModal);
    deleteConfirmBtn.addEventListener('click', confirmDelete);

    // Keyboard: Escape closes any open modal
    document.addEventListener('keydown', e => {
        if (e.key !== 'Escape') return;
        if (formModal.classList.contains('open'))   closeFormModal();
        if (deleteModal.classList.contains('open')) closeDeleteModal();
    });
}

// ─── API: Load Students ────────────────────────────────────────────────────────
async function loadStudents(isRefresh = false) {
    showLoadingState();
    refreshBtn.disabled = true;

    try {
        const res = await fetch(API);
        if (!res.ok) throw new Error(`Server error (${res.status})`);

        allStudents = await res.json();
        applySearch();
        updateStats(allStudents);

        if (isRefresh) showToast('Student list refreshed', 'info');
    } catch (err) {
        console.error('Load error:', err);
        showErrorState(err.message);
        showToast('Could not load students: ' + err.message, 'error');
    } finally {
        refreshBtn.disabled = false;
    }
}

// ─── Search & Filter ───────────────────────────────────────────────────────────
function applySearch() {
    const q = searchInput.value.trim().toLowerCase();

    filteredList = q
        ? allStudents.filter(s =>
            s.name.toLowerCase().includes(q) ||
            s.email.toLowerCase().includes(q) ||
            s.course.toLowerCase().includes(q))
        : [...allStudents];

    renderStudents(filteredList);
}

// ─── Stats ─────────────────────────────────────────────────────────────────────
function updateStats(students) {
    statTotal.textContent = students.length;

    const uniqueCourses = new Set(students.map(s => s.course.toLowerCase())).size;
    statCourses.textContent = uniqueCourses;

    if (students.length > 0) {
        const avg = students.reduce((sum, s) => sum + s.age, 0) / students.length;
        statAvgAge.textContent = avg.toFixed(1);
    } else {
        statAvgAge.textContent = '—';
    }
}

// ─── Render Students ───────────────────────────────────────────────────────────
function renderStudents(students) {
    stateContainer.innerHTML = '';
    tableBody.innerHTML = '';
    mobileCards.innerHTML = '';

    if (students.length === 0) {
        const isFiltering = searchInput.value.trim().length > 0;
        showEmptyState(isFiltering);
        return;
    }

    students.forEach(s => {
        // Desktop table row
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td class="cell-id">#${s.id}</td>
            <td class="cell-name">${esc(s.name)}</td>
            <td class="cell-email">${esc(s.email)}</td>
            <td class="cell-course"><span class="tag">${esc(s.course)}</span></td>
            <td class="cell-age">${s.age}</td>
            <td>
                <div class="row-actions">
                    <button class="btn btn-ghost btn-sm edit-btn" aria-label="Edit ${esc(s.name)}">Edit</button>
                    <button class="btn btn-danger btn-sm del-btn"  aria-label="Delete ${esc(s.name)}">Delete</button>
                </div>
            </td>`;
        tr.querySelector('.edit-btn').addEventListener('click', () => openEditModal(s));
        tr.querySelector('.del-btn').addEventListener('click',  () => openDeleteModal(s.id, s.name));
        tableBody.appendChild(tr);

        // Mobile card
        const card = document.createElement('div');
        card.className = 'mobile-card';
        card.innerHTML = `
            <div class="mobile-card-top">
                <div>
                    <div class="mobile-card-name">${esc(s.name)}</div>
                    <div class="mobile-card-email">${esc(s.email)}</div>
                </div>
                <span class="cell-id">#${s.id}</span>
            </div>
            <div class="mobile-card-meta">
                <span class="tag">${esc(s.course)}</span>
                <span style="color:var(--color-text-muted);font-size:.82rem;">${s.age} yrs</span>
            </div>
            <div class="mobile-card-actions">
                <button class="btn btn-secondary btn-sm me-btn">Edit</button>
                <button class="btn btn-danger  btn-sm md-btn">Delete</button>
            </div>`;
        card.querySelector('.me-btn').addEventListener('click', () => openEditModal(s));
        card.querySelector('.md-btn').addEventListener('click', () => openDeleteModal(s.id, s.name));
        mobileCards.appendChild(card);
    });
}

// ─── State Views ───────────────────────────────────────────────────────────────
function showLoadingState() {
    tableBody.innerHTML = '';
    mobileCards.innerHTML = '';
    stateContainer.innerHTML = `
        <div class="state-container">
            <div class="spinner"></div>
            <p class="state-desc">Loading students…</p>
        </div>`;
}

function showEmptyState(isFiltering) {
    stateContainer.innerHTML = isFiltering
        ? `<div class="state-container">
               <div class="state-icon">🔍</div>
               <div class="state-title">No results found</div>
               <p class="state-desc">No students match your search. Try a different name, email or course.</p>
           </div>`
        : `<div class="state-container">
               <div class="state-icon">🎓</div>
               <div class="state-title">No students yet</div>
               <p class="state-desc">Get started by adding your first student record.</p>
               <button class="btn btn-primary btn-md" style="margin-top:12px;" id="empty-add-btn">+ Add Student</button>
           </div>`;

    const emptyAddBtn = document.getElementById('empty-add-btn');
    if (emptyAddBtn) emptyAddBtn.addEventListener('click', openAddModal);
}

function showErrorState(message) {
    tableBody.innerHTML = '';
    mobileCards.innerHTML = '';
    stateContainer.innerHTML = `
        <div class="state-container">
            <div class="state-icon">⚠️</div>
            <div class="state-title">Failed to load students</div>
            <p class="state-desc">${esc(message)}</p>
            <button class="btn btn-primary btn-md" style="margin-top:12px;" id="retry-btn">Retry</button>
        </div>`;
    document.getElementById('retry-btn')?.addEventListener('click', () => loadStudents());
}

// ─── Add / Edit Modal ──────────────────────────────────────────────────────────
function openAddModal() {
    resetForm();
    formModalTitle.textContent = 'Add Student';
    submitLabel.textContent    = 'Add Student';
    openModal(formModal);
    fieldName.focus();
}

function openEditModal(s) {
    resetForm();
    fieldId.value     = s.id;
    fieldName.value   = s.name;
    fieldEmail.value  = s.email;
    fieldCourse.value = s.course;
    fieldAge.value    = s.age;

    formModalTitle.textContent = `Edit Student #${s.id}`;
    submitLabel.textContent    = 'Update Student';
    openModal(formModal);
    fieldName.focus();
}

function closeFormModal() {
    closeModal(formModal);
    resetForm();
}

function resetForm() {
    studentForm.reset();
    fieldId.value = '';
    clearErrors();
}

// ─── Form Validation ───────────────────────────────────────────────────────────
const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function validateField(name) {
    switch (name) {
        case 'name': {
            const v = fieldName.value.trim();
            if (!v)          return setError(fieldName, errName, 'Name is required.');
            if (v.length < 2)return setError(fieldName, errName, 'Name must be at least 2 characters.');
            return clearError(fieldName, errName);
        }
        case 'email': {
            const v = fieldEmail.value.trim();
            if (!v)                 return setError(fieldEmail, errEmail, 'Email is required.');
            if (!EMAIL_RE.test(v))  return setError(fieldEmail, errEmail, 'Enter a valid email address.');
            return clearError(fieldEmail, errEmail);
        }
        case 'course': {
            const v = fieldCourse.value.trim();
            if (!v) return setError(fieldCourse, errCourse, 'Course is required.');
            return clearError(fieldCourse, errCourse);
        }
        case 'age': {
            const v = parseInt(fieldAge.value, 10);
            if (!fieldAge.value.trim())  return setError(fieldAge, errAge, 'Age is required.');
            if (isNaN(v) || v < 10 || v > 100)
                return setError(fieldAge, errAge, 'Age must be between 10 and 100.');
            return clearError(fieldAge, errAge);
        }
    }
    return true;
}

function validateAll() {
    const ok = ['name','email','course','age'].map(validateField);
    return ok.every(Boolean);
}

function setError(input, errEl, msg) {
    input.classList.add('is-error');
    errEl.textContent = msg;
    return false;
}

function clearError(input, errEl) {
    input.classList.remove('is-error');
    errEl.textContent = '';
    return true;
}

function clearErrors() {
    [fieldName, fieldEmail, fieldCourse, fieldAge].forEach(el => el.classList.remove('is-error'));
    [errName, errEmail, errCourse, errAge].forEach(el => el.textContent = '');
}

// ─── Form Submit (POST or PUT) ─────────────────────────────────────────────────
async function handleFormSubmit() {
    if (!validateAll()) {
        showToast('Please fix the errors in the form.', 'error');
        return;
    }
    if (isSubmitting) return;
    isSubmitting = true;

    const isEdit = !!fieldId.value;
    const payload = {
        name:   fieldName.value.trim(),
        email:  fieldEmail.value.trim(),
        course: fieldCourse.value.trim(),
        age:    parseInt(fieldAge.value, 10)
    };

    const url    = isEdit ? `${API}/${fieldId.value}` : API;
    const method = isEdit ? 'PUT' : 'POST';

    formSubmitBtn.disabled   = true;
    submitLabel.textContent  = isEdit ? 'Saving…' : 'Adding…';

    try {
        const res = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || `Server error (${res.status})`);
        }

        const saved = await res.json();
        showToast(
            isEdit
                ? `Student #${saved.id} updated successfully.`
                : `"${saved.name}" added successfully.`,
            'success'
        );
        closeFormModal();
        await loadStudents();
    } catch (err) {
        console.error('Submit error:', err);
        showToast('Save failed: ' + err.message, 'error');
    } finally {
        isSubmitting          = false;
        formSubmitBtn.disabled = false;
        submitLabel.textContent = fieldId.value ? 'Update Student' : 'Add Student';
    }
}

// ─── Delete Confirm Modal ──────────────────────────────────────────────────────
function openDeleteModal(id, name) {
    pendingDelete = { id, name };
    deleteStudentName.textContent = `"${name}" (ID #${id})`;
    openModal(deleteModal);
    deleteConfirmBtn.focus();
}

function closeDeleteModal() {
    pendingDelete = null;
    closeModal(deleteModal);
}

async function confirmDelete() {
    if (!pendingDelete) return;
    const { id, name } = pendingDelete;

    deleteConfirmBtn.disabled    = true;
    deleteConfirmBtn.textContent = 'Deleting…';

    try {
        const res = await fetch(`${API}/${id}`, { method: 'DELETE' });

        if (res.status === 204) {
            showToast(`"${name}" deleted successfully.`, 'success');
            // If currently editing this student, close the form
            if (fieldId.value == id) closeFormModal();
            await loadStudents();
        } else if (res.status === 404) {
            showToast(`Student #${id} was not found.`, 'error');
            await loadStudents();
        } else {
            throw new Error(`Server error (${res.status})`);
        }
    } catch (err) {
        console.error('Delete error:', err);
        showToast('Delete failed: ' + err.message, 'error');
    } finally {
        deleteConfirmBtn.disabled    = false;
        deleteConfirmBtn.textContent = 'Delete';
        closeDeleteModal();
    }
}

// ─── Modal Helpers ─────────────────────────────────────────────────────────────
function openModal(overlay) {
    overlay.classList.add('open');
    document.body.style.overflow = 'hidden';
}

function closeModal(overlay) {
    overlay.classList.remove('open');
    document.body.style.overflow = '';
}

// ─── Toast Notifications ───────────────────────────────────────────────────────
function showToast(message, type = 'info') {
    const icons = { success: '✓', error: '✕', info: 'ℹ' };
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
        <span class="toast-icon" aria-hidden="true">${icons[type] ?? 'ℹ'}</span>
        <span class="toast-text">${esc(message)}</span>`;
    toastContainer.appendChild(toast);

    setTimeout(() => {
        toast.classList.add('out');
        toast.addEventListener('animationend', () => toast.remove(), { once: true });
    }, 4000);
}

// ─── XSS Safety ───────────────────────────────────────────────────────────────
function esc(str) {
    return String(str ?? '').replace(/[&<>"']/g, c => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    })[c]);
}
