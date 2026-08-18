/**
 * Student Management System — Modern Enterprise Client
 * 
 * Communicates with Spring Boot REST endpoints at /api/students.
 * Features: Multi-criteria filtering, column sorting, client pagination,
 * bulk actions, CSV export, light/dark theme persistence, and details modal.
 */

'use strict';

const API = '/api/students';

// ─── Application State ────────────────────────────────────────────────────────
const state = {
    students: [],         // Raw dataset from backend
    filtered: [],         // After search + course filter applied
    searchQuery: '',
    courseFilter: '',
    sortColumn: 'id',
    sortDirection: 'asc', // 'asc' | 'desc'
    currentPage: 1,
    pageSize: 10,
    selectedIds: new Set(),
    pendingDelete: null,  // { id, name } or 'bulk'
    activeDetailStudent: null,
    isSubmitting: false,
    theme: localStorage.getItem('theme') || (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light')
};

// ─── DOM References ────────────────────────────────────────────────────────────
const $ = id => document.getElementById(id);

// Stats Elements
const statTotal       = $('stat-total');
const statCourses     = $('stat-courses');
const statAvgAge      = $('stat-avg-age');
const statActiveRate  = $('stat-active-rate');

// Toolbar & Controls
const searchInput     = $('search-input');
const searchClearBtn  = $('search-clear-btn');
const courseSelect    = $('course-filter-select');
const refreshBtn      = $('refresh-btn');
const exportCsvBtn    = $('export-csv-btn');
const themeToggleBtn  = $('theme-toggle-btn');
const sunIcon         = $('theme-icon-sun');
const moonIcon        = $('theme-icon-moon');

// Table & Content
const tableBody       = $('table-body');
const mobileCards     = $('mobile-cards');
const stateContainer  = $('state-container');
const selectAllCb     = $('select-all-checkbox');
const bulkActionsBar  = $('bulk-actions-bar');
const bulkCountSpan   = $('bulk-selected-count');
const bulkDeleteBtn   = $('bulk-delete-btn');

// Pagination Elements
const paginationBar   = $('pagination-bar');
const pageRangeText   = $('page-range');
const pageTotalText   = $('page-total');
const pageSizeSelect  = $('page-size-select');
const prevPageBtn     = $('prev-page-btn');
const nextPageBtn     = $('next-page-btn');
const pageNumbersDiv  = $('page-numbers');

// Add / Edit Modal
const formModal       = $('student-form-modal');
const formModalTitle  = $('form-modal-title');
const formCloseBtn    = $('form-modal-close-btn');
const formCancelBtn   = $('form-cancel-btn');
const formSubmitBtn   = $('form-submit-btn');
const submitLabel     = $('submit-label');
const studentForm     = $('student-form');

// Form Fields & Errors
const fieldId         = $('field-id');
const fieldName       = $('field-name');
const fieldEmail      = $('field-email');
const fieldCourse     = $('field-course');
const fieldAge        = $('field-age');
const errName         = $('err-name');
const errEmail        = $('err-email');
const errCourse       = $('err-course');
const errAge          = $('err-age');

// Details Modal
const detailsModal    = $('details-modal');
const detailsCloseBtn = $('details-modal-close-btn');
const detailsCloseBtn2= $('details-close-btn');
const detailsEditBtn  = $('details-edit-btn');
const detailAvatar    = $('detail-avatar');
const detailName      = $('detail-name');
const detailId        = $('detail-id');
const detailEmail     = $('detail-email');
const detailCourse    = $('detail-course');
const detailAge       = $('detail-age');

// Delete Confirm Modal
const deleteModal     = $('delete-modal');
const deleteTitle     = $('delete-modal-title');
const deleteStudentName = $('delete-student-name');
const deleteCancelBtn = $('delete-cancel-btn');
const deleteConfirmBtn= $('delete-confirm-btn');

// Toast Container
const toastContainer  = $('toast-container');

// ─── Initialization ───────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    applyTheme(state.theme);
    bindEventListeners();
    loadStudents();
});

// ─── Event Binding ────────────────────────────────────────────────────────────
function bindEventListeners() {
    // Theme Toggle
    themeToggleBtn.addEventListener('click', toggleTheme);

    // Primary Actions
    $('open-add-modal-btn').addEventListener('click', openAddModal);
    refreshBtn.addEventListener('click', () => loadStudents(true));
    exportCsvBtn.addEventListener('click', exportToCsv);

    // Search & Filter
    searchInput.addEventListener('input', () => {
        state.searchQuery = searchInput.value.trim().toLowerCase();
        searchClearBtn.classList.toggle('visible', !!searchInput.value);
        state.currentPage = 1;
        applyFiltersAndRender();
    });

    searchClearBtn.addEventListener('click', () => {
        searchInput.value = '';
        state.searchQuery = '';
        searchClearBtn.classList.remove('visible');
        state.currentPage = 1;
        applyFiltersAndRender();
        searchInput.focus();
    });

    courseSelect.addEventListener('change', () => {
        state.courseFilter = courseSelect.value;
        state.currentPage = 1;
        applyFiltersAndRender();
    });

    // Column Sorting Headers
    document.querySelectorAll('.data-table thead th.sortable').forEach(th => {
        th.addEventListener('click', () => {
            const column = th.dataset.sort;
            if (state.sortColumn === column) {
                state.sortDirection = state.sortDirection === 'asc' ? 'desc' : 'asc';
            } else {
                state.sortColumn = column;
                state.sortDirection = 'asc';
            }
            updateSortHeaderStyles();
            applyFiltersAndRender();
        });
    });

    // Pagination
    pageSizeSelect.addEventListener('change', e => {
        state.pageSize = parseInt(e.target.value, 10);
        state.currentPage = 1;
        renderPaginatedView();
    });

    prevPageBtn.addEventListener('click', () => {
        if (state.currentPage > 1) {
            state.currentPage--;
            renderPaginatedView();
        }
    });

    nextPageBtn.addEventListener('click', () => {
        const totalPages = Math.ceil(state.filtered.length / state.pageSize) || 1;
        if (state.currentPage < totalPages) {
            state.currentPage++;
            renderPaginatedView();
        }
    });

    // Bulk Actions
    selectAllCb.addEventListener('change', e => {
        const isChecked = e.target.checked;
        const pageItems = getCurrentPageItems();
        pageItems.forEach(s => {
            if (isChecked) state.selectedIds.add(s.id);
            else state.selectedIds.delete(s.id);
        });
        updateBulkActionsBar();
        renderTableRows();
    });

    bulkDeleteBtn.addEventListener('click', openBulkDeleteModal);

    // Form Modals
    formCloseBtn.addEventListener('click', closeFormModal);
    formCancelBtn.addEventListener('click', closeFormModal);
    formSubmitBtn.addEventListener('click', handleFormSubmit);

    // Live Validations
    fieldName.addEventListener('input', () => validateField('name'));
    fieldEmail.addEventListener('input', () => validateField('email'));
    fieldCourse.addEventListener('input', () => validateField('course'));
    fieldAge.addEventListener('input', () => validateField('age'));

    // Details Modal
    detailsCloseBtn.addEventListener('click', closeDetailsModal);
    detailsCloseBtn2.addEventListener('click', closeDetailsModal);
    detailsEditBtn.addEventListener('click', () => {
        if (state.activeDetailStudent) {
            closeDetailsModal();
            openEditModal(state.activeDetailStudent);
        }
    });

    // Delete Modal
    deleteCancelBtn.addEventListener('click', closeDeleteModal);
    deleteConfirmBtn.addEventListener('click', confirmDelete);

    // Modal Keyboard Shortcuts
    document.addEventListener('keydown', e => {
        if (e.key === 'Escape') {
            if (formModal.classList.contains('open')) closeFormModal();
            if (detailsModal.classList.contains('open')) closeDetailsModal();
            if (deleteModal.classList.contains('open')) closeDeleteModal();
        }
    });
}

// ─── Theme Management ─────────────────────────────────────────────────────────
function applyTheme(theme) {
    state.theme = theme;
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);

    if (theme === 'dark') {
        sunIcon.style.display = 'block';
        moonIcon.style.display = 'none';
    } else {
        sunIcon.style.display = 'none';
        moonIcon.style.display = 'block';
    }
}

function toggleTheme() {
    const nextTheme = state.theme === 'dark' ? 'light' : 'dark';
    applyTheme(nextTheme);
}

// ─── Data Fetching ────────────────────────────────────────────────────────────
async function loadStudents(isManualRefresh = false) {
    showLoadingSkeleton();
    refreshBtn.disabled = true;

    try {
        const res = await fetch(API);
        if (!res.ok) throw new Error(`Server responded with status ${res.status}`);

        state.students = await res.json();
        updateCourseDropdown(state.students);
        updateStats(state.students);
        applyFiltersAndRender();

        if (isManualRefresh) {
            showToast('Student directory refreshed', 'info');
        }
    } catch (err) {
        console.error('Failed to load students:', err);
        showErrorState(err.message || 'Unable to connect to server');
        showToast('Error loading students: ' + err.message, 'error');
    } finally {
        refreshBtn.disabled = false;
    }
}

// ─── Course Dropdown Population ───────────────────────────────────────────────
function updateCourseDropdown(students) {
    const currentVal = courseSelect.value;
    const courses = [...new Set(students.map(s => s.course.trim()).filter(Boolean))].sort();

    courseSelect.innerHTML = '<option value="">All Courses</option>';
    courses.forEach(c => {
        const opt = document.createElement('option');
        opt.value = c;
        opt.textContent = c;
        if (c === currentVal) opt.selected = true;
        courseSelect.appendChild(opt);
    });
}

// ─── Statistics Calculation ───────────────────────────────────────────────────
function updateStats(students) {
    statTotal.textContent = students.length;

    const uniqueCourses = new Set(students.map(s => s.course.toLowerCase().trim())).size;
    statCourses.textContent = uniqueCourses;

    if (students.length > 0) {
        const avg = students.reduce((acc, s) => acc + (s.age || 0), 0) / students.length;
        statAvgAge.textContent = avg.toFixed(1);
    } else {
        statAvgAge.textContent = '—';
    }

    statActiveRate.textContent = students.length > 0 ? '100%' : '0%';
}

// ─── Filtering & Sorting ──────────────────────────────────────────────────────
function applyFiltersAndRender() {
    let list = [...state.students];

    // Search query filter
    if (state.searchQuery) {
        const q = state.searchQuery;
        list = list.filter(s =>
            (s.name && s.name.toLowerCase().includes(q)) ||
            (s.email && s.email.toLowerCase().includes(q)) ||
            (s.course && s.course.toLowerCase().includes(q)) ||
            String(s.id).includes(q)
        );
    }

    // Course filter
    if (state.courseFilter) {
        list = list.filter(s => s.course && s.course.trim() === state.courseFilter);
    }

    // Sorting
    list.sort((a, b) => {
        let fieldA = a[state.sortColumn];
        let fieldB = b[state.sortColumn];

        if (typeof fieldA === 'string') fieldA = fieldA.toLowerCase();
        if (typeof fieldB === 'string') fieldB = fieldB.toLowerCase();

        if (fieldA < fieldB) return state.sortDirection === 'asc' ? -1 : 1;
        if (fieldA > fieldB) return state.sortDirection === 'asc' ? 1 : -1;
        return 0;
    });

    state.filtered = list;
    renderPaginatedView();
}

function updateSortHeaderStyles() {
    document.querySelectorAll('.data-table thead th.sortable').forEach(th => {
        th.classList.remove('sorted-asc', 'sorted-desc');
        const icon = th.querySelector('.sort-icon');
        if (icon) icon.textContent = '⇅';

        if (th.dataset.sort === state.sortColumn) {
            th.classList.add(state.sortDirection === 'asc' ? 'sorted-asc' : 'sorted-desc');
            if (icon) icon.textContent = state.sortDirection === 'asc' ? '↑' : '↓';
        }
    });
}

// ─── Pagination & View Rendering ──────────────────────────────────────────────
function getCurrentPageItems() {
    const start = (state.currentPage - 1) * state.pageSize;
    return state.filtered.slice(start, start + state.pageSize);
}

function renderPaginatedView() {
    stateContainer.innerHTML = '';
    tableBody.innerHTML = '';
    mobileCards.innerHTML = '';

    const total = state.filtered.length;
    const totalPages = Math.ceil(total / state.pageSize) || 1;

    if (state.currentPage > totalPages) state.currentPage = totalPages;
    if (state.currentPage < 1) state.currentPage = 1;

    // Handle Empty States
    if (total === 0) {
        paginationBar.style.display = 'none';
        const isFiltered = state.searchQuery || state.courseFilter;
        showEmptyState(isFiltered);
        return;
    }

    paginationBar.style.display = 'flex';

    // Update Pagination Info
    const startIdx = (state.currentPage - 1) * state.pageSize + 1;
    const endIdx = Math.min(state.currentPage * state.pageSize, total);
    pageRangeText.textContent = `${startIdx}-${endIdx}`;
    pageTotalText.textContent = total;

    prevPageBtn.disabled = state.currentPage === 1;
    nextPageBtn.disabled = state.currentPage === totalPages;

    renderPageNumberButtons(totalPages);
    renderTableRows();
    renderMobileCards();
    updateBulkActionsBar();
}

function renderPageNumberButtons(totalPages) {
    pageNumbersDiv.innerHTML = '';
    const maxButtons = 5;
    let startPage = Math.max(1, state.currentPage - Math.floor(maxButtons / 2));
    let endPage = Math.min(totalPages, startPage + maxButtons - 1);

    if (endPage - startPage + 1 < maxButtons) {
        startPage = Math.max(1, endPage - maxButtons + 1);
    }

    for (let p = startPage; p <= endPage; p++) {
        const btn = document.createElement('button');
        btn.className = `page-btn ${p === state.currentPage ? 'active' : ''}`;
        btn.textContent = p;
        btn.addEventListener('click', () => {
            state.currentPage = p;
            renderPaginatedView();
        });
        pageNumbersDiv.appendChild(btn);
    }
}

// ─── Table & Mobile Card Rendering ────────────────────────────────────────────
function renderTableRows() {
    tableBody.innerHTML = '';
    const items = getCurrentPageItems();

    items.forEach(student => {
        const isSelected = state.selectedIds.has(student.id);
        const initials = getInitials(student.name);

        const tr = document.createElement('tr');
        if (isSelected) tr.classList.add('selected');

        tr.innerHTML = `
            <td class="checkbox-cell">
                <input type="checkbox" class="custom-checkbox row-select-cb" data-id="${student.id}" ${isSelected ? 'checked' : ''} aria-label="Select student">
            </td>
            <td>
                <span class="student-id-badge">#${student.id}</span>
            </td>
            <td>
                <div class="student-identity">
                    <div class="student-avatar" aria-hidden="true">${initials}</div>
                    <span class="student-name-text">${escapeHtml(student.name)}</span>
                </div>
            </td>
            <td>
                <span class="student-email-sub">${escapeHtml(student.email)}</span>
            </td>
            <td>
                <span class="course-tag" title="${escapeHtml(student.course)}">
                    ${escapeHtml(student.course)}
                </span>
            </td>
            <td>
                <span class="age-badge">${student.age} yrs</span>
            </td>
            <td>
                <div class="row-actions">
                    <button class="action-icon-btn btn-view" title="View Profile" aria-label="View ${escapeHtml(student.name)}">
                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                            <circle cx="12" cy="12" r="3"></circle>
                        </svg>
                    </button>
                    <button class="action-icon-btn btn-edit" title="Edit Student" aria-label="Edit ${escapeHtml(student.name)}">
                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                        </svg>
                    </button>
                    <button class="action-icon-btn btn-delete" title="Delete Student" aria-label="Delete ${escapeHtml(student.name)}">
                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <polyline points="3 6 5 6 21 6"></polyline>
                            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                        </svg>
                    </button>
                </div>
            </td>
        `;

        // Event bindings for row
        const rowCb = tr.querySelector('.row-select-cb');
        rowCb.addEventListener('change', e => {
            if (e.target.checked) state.selectedIds.add(student.id);
            else state.selectedIds.delete(student.id);
            updateBulkActionsBar();
            tr.classList.toggle('selected', e.target.checked);
        });

        tr.querySelector('.btn-view').addEventListener('click', () => openDetailsModal(student));
        tr.querySelector('.btn-edit').addEventListener('click', () => openEditModal(student));
        tr.querySelector('.btn-delete').addEventListener('click', () => openSingleDeleteModal(student));

        tableBody.appendChild(tr);
    });

    // Update select-all checkbox state
    const pageIds = items.map(s => s.id);
    const allSelected = pageIds.length > 0 && pageIds.every(id => state.selectedIds.has(id));
    selectAllCb.checked = allSelected;
}

function renderMobileCards() {
    mobileCards.innerHTML = '';
    const items = getCurrentPageItems();

    items.forEach(student => {
        const card = document.createElement('div');
        card.className = 'mobile-card-item';
        card.innerHTML = `
            <div class="mobile-card-top">
                <div class="student-identity">
                    <div class="student-avatar">${getInitials(student.name)}</div>
                    <div>
                        <div class="student-name-text">${escapeHtml(student.name)}</div>
                        <div class="student-email-sub">${escapeHtml(student.email)}</div>
                    </div>
                </div>
                <span class="student-id-badge">#${student.id}</span>
            </div>
            <div class="mobile-card-details">
                <span class="course-tag">${escapeHtml(student.course)}</span>
                <span class="age-badge">${student.age} yrs</span>
            </div>
            <div class="mobile-card-bottom">
                <button class="btn btn-secondary btn-sm mob-view-btn">View</button>
                <button class="btn btn-secondary btn-sm mob-edit-btn">Edit</button>
                <button class="btn btn-danger btn-sm mob-del-btn">Delete</button>
            </div>
        `;

        card.querySelector('.mob-view-btn').addEventListener('click', () => openDetailsModal(student));
        card.querySelector('.mob-edit-btn').addEventListener('click', () => openEditModal(student));
        card.querySelector('.mob-del-btn').addEventListener('click', () => openSingleDeleteModal(student));

        mobileCards.appendChild(card);
    });
}

// ─── Bulk Action Bar Handling ─────────────────────────────────────────────────
function updateBulkActionsBar() {
    const count = state.selectedIds.size;
    if (count > 0) {
        bulkActionsBar.classList.add('active');
        bulkCountSpan.textContent = `${count} student${count > 1 ? 's' : ''} selected`;
    } else {
        bulkActionsBar.classList.remove('active');
    }
}

// ─── State Loaders & Views ────────────────────────────────────────────────────
function showLoadingSkeleton() {
    tableBody.innerHTML = '';
    mobileCards.innerHTML = '';
    stateContainer.innerHTML = '';
    paginationBar.style.display = 'none';

    for (let i = 0; i < 5; i++) {
        const tr = document.createElement('tr');
        tr.className = 'skeleton-row';
        tr.innerHTML = `
            <td class="checkbox-cell"><div class="skeleton" style="width:16px;height:16px;"></div></td>
            <td><div class="skeleton skeleton-text" style="width:30px;"></div></td>
            <td>
                <div class="student-identity">
                    <div class="skeleton skeleton-avatar"></div>
                    <div class="skeleton skeleton-text" style="width:120px;"></div>
                </div>
            </td>
            <td><div class="skeleton skeleton-text" style="width:160px;"></div></td>
            <td><div class="skeleton skeleton-text" style="width:100px;"></div></td>
            <td><div class="skeleton skeleton-text" style="width:40px;"></div></td>
            <td><div class="skeleton skeleton-text" style="width:80px;float:right;"></div></td>
        `;
        tableBody.appendChild(tr);
    }
}

function showEmptyState(isFiltered) {
    stateContainer.innerHTML = `
        <div class="state-box">
            <div class="state-illustration">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="11" cy="11" r="8"></circle>
                    <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                </svg>
            </div>
            <h3 class="state-title">${isFiltered ? 'No matching student records found' : 'No students enrolled yet'}</h3>
            <p class="state-description">
                ${isFiltered 
                    ? 'No records match your active search or course filter. Try adjusting your search criteria.' 
                    : 'Get started by creating your first student record in the database.'}
            </p>
            ${isFiltered 
                ? '<button id="reset-filters-btn" class="btn btn-secondary btn-md" style="margin-top:0.5rem;">Reset Filters</button>' 
                : '<button id="empty-add-btn" class="btn btn-primary btn-md" style="margin-top:0.5rem;">+ Add Student</button>'}
        </div>
    `;

    $('reset-filters-btn')?.addEventListener('click', () => {
        searchInput.value = '';
        state.searchQuery = '';
        searchClearBtn.classList.remove('visible');
        courseSelect.value = '';
        state.courseFilter = '';
        state.currentPage = 1;
        applyFiltersAndRender();
    });

    $('empty-add-btn')?.addEventListener('click', openAddModal);
}

function showErrorState(errorMessage) {
    tableBody.innerHTML = '';
    mobileCards.innerHTML = '';
    paginationBar.style.display = 'none';

    stateContainer.innerHTML = `
        <div class="state-box">
            <div class="state-illustration" style="background:var(--color-danger-bg);color:var(--color-danger);">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"></circle>
                    <line x1="12" y1="8" x2="12" y2="12"></line>
                    <line x1="12" y1="16" x2="12.01" y2="16"></line>
                </svg>
            </div>
            <h3 class="state-title">Unable to load student directory</h3>
            <p class="state-description">${escapeHtml(errorMessage)}</p>
            <button id="retry-btn" class="btn btn-primary btn-md" style="margin-top:0.5rem;">Retry Connection</button>
        </div>
    `;

    $('retry-btn')?.addEventListener('click', () => loadStudents());
}

// ─── Add & Edit Form Handling ─────────────────────────────────────────────────
function openAddModal() {
    resetForm();
    formModalTitle.textContent = 'Add New Student';
    submitLabel.textContent = 'Add Student';
    openModal(formModal);
    fieldName.focus();
}

function openEditModal(student) {
    resetForm();
    fieldId.value = student.id;
    fieldName.value = student.name;
    fieldEmail.value = student.email;
    fieldCourse.value = student.course;
    fieldAge.value = student.age;

    formModalTitle.textContent = `Edit Student #${student.id}`;
    submitLabel.textContent = 'Update Student';
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
    clearAllErrors();
}

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function validateField(name) {
    switch (name) {
        case 'name': {
            const v = fieldName.value.trim();
            if (!v) return setFieldError(fieldName, errName, 'Full name is required.');
            if (v.length < 2) return setFieldError(fieldName, errName, 'Name must be at least 2 characters.');
            return clearFieldError(fieldName, errName);
        }
        case 'email': {
            const v = fieldEmail.value.trim();
            if (!v) return setFieldError(fieldEmail, errEmail, 'Email address is required.');
            if (!EMAIL_REGEX.test(v)) return setFieldError(fieldEmail, errEmail, 'Enter a valid email address.');
            return clearFieldError(fieldEmail, errEmail);
        }
        case 'course': {
            const v = fieldCourse.value.trim();
            if (!v) return setFieldError(fieldCourse, errCourse, 'Course/Department is required.');
            return clearFieldError(fieldCourse, errCourse);
        }
        case 'age': {
            const val = parseInt(fieldAge.value, 10);
            if (!fieldAge.value.trim()) return setFieldError(fieldAge, errAge, 'Age is required.');
            if (isNaN(val) || val < 10 || val > 100) return setFieldError(fieldAge, errAge, 'Age must be between 10 and 100.');
            return clearFieldError(fieldAge, errAge);
        }
    }
    return true;
}

function validateAllFields() {
    const results = ['name', 'email', 'course', 'age'].map(validateField);
    return results.every(Boolean);
}

function setFieldError(input, errorSpan, message) {
    input.classList.add('is-invalid');
    errorSpan.textContent = message;
    return false;
}

function clearFieldError(input, errorSpan) {
    input.classList.remove('is-invalid');
    errorSpan.textContent = '';
    return true;
}

function clearAllErrors() {
    [fieldName, fieldEmail, fieldCourse, fieldAge].forEach(input => input.classList.remove('is-invalid'));
    [errName, errEmail, errCourse, errAge].forEach(span => span.textContent = '');
}

async function handleFormSubmit() {
    if (!validateAllFields()) {
        showToast('Please fix the validation errors in the form.', 'error');
        return;
    }
    if (state.isSubmitting) return;
    state.isSubmitting = true;

    const isEdit = !!fieldId.value;
    const payload = {
        name: fieldName.value.trim(),
        email: fieldEmail.value.trim(),
        course: fieldCourse.value.trim(),
        age: parseInt(fieldAge.value, 10)
    };

    const targetUrl = isEdit ? `${API}/${fieldId.value}` : API;
    const httpMethod = isEdit ? 'PUT' : 'POST';

    formSubmitBtn.disabled = true;
    submitLabel.textContent = isEdit ? 'Saving…' : 'Adding…';

    try {
        const response = await fetch(targetUrl, {
            method: httpMethod,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const errorPayload = await response.json().catch(() => ({}));
            throw new Error(errorPayload.message || `Request failed with status ${response.status}`);
        }

        const savedStudent = await response.json();
        showToast(
            isEdit ? `Student #${savedStudent.id} updated successfully.` : `Student "${savedStudent.name}" enrolled successfully.`,
            'success'
        );

        closeFormModal();
        await loadStudents();
    } catch (err) {
        console.error('Submit failure:', err);
        showToast(`Save failed: ${err.message}`, 'error');
    } finally {
        state.isSubmitting = false;
        formSubmitBtn.disabled = false;
        submitLabel.textContent = fieldId.value ? 'Update Student' : 'Add Student';
    }
}

// ─── Details View Modal ───────────────────────────────────────────────────────
function openDetailsModal(student) {
    state.activeDetailStudent = student;
    detailAvatar.textContent = getInitials(student.name);
    detailName.textContent = student.name;
    detailId.textContent = `#${student.id}`;
    detailEmail.textContent = student.email;
    detailCourse.textContent = student.course;
    detailAge.textContent = `${student.age} years old`;

    openModal(detailsModal);
}

function closeDetailsModal() {
    state.activeDetailStudent = null;
    closeModal(detailsModal);
}

// ─── Deletion Handling (Single & Bulk) ────────────────────────────────────────
function openSingleDeleteModal(student) {
    state.pendingDelete = { type: 'single', id: student.id, name: student.name };
    deleteTitle.textContent = 'Delete Student Record?';
    deleteStudentName.textContent = `"${student.name}" (ID #${student.id})`;
    openModal(deleteModal);
    deleteConfirmBtn.focus();
}

function openBulkDeleteModal() {
    const count = state.selectedIds.size;
    if (count === 0) return;

    state.pendingDelete = { type: 'bulk', count };
    deleteTitle.textContent = `Delete ${count} Selected Record${count > 1 ? 's' : ''}?`;
    deleteStudentName.textContent = `${count} selected student records`;
    openModal(deleteModal);
    deleteConfirmBtn.focus();
}

function closeDeleteModal() {
    state.pendingDelete = null;
    closeModal(deleteModal);
}

async function confirmDelete() {
    if (!state.pendingDelete) return;

    deleteConfirmBtn.disabled = true;
    deleteConfirmBtn.textContent = 'Deleting…';

    try {
        if (state.pendingDelete.type === 'single') {
            const { id, name } = state.pendingDelete;
            const res = await fetch(`${API}/${id}`, { method: 'DELETE' });

            if (res.status === 204) {
                showToast(`"${name}" deleted successfully.`, 'success');
                state.selectedIds.delete(id);
                if (fieldId.value == id) closeFormModal();
            } else if (res.status === 404) {
                showToast(`Student #${id} not found.`, 'error');
            } else {
                throw new Error(`Server returned status ${res.status}`);
            }
        } else if (state.pendingDelete.type === 'bulk') {
            const idsToDelete = Array.from(state.selectedIds);
            let deletedCount = 0;

            for (const id of idsToDelete) {
                try {
                    const res = await fetch(`${API}/${id}`, { method: 'DELETE' });
                    if (res.status === 204) deletedCount++;
                } catch (e) {
                    console.error(`Failed to delete student ${id}:`, e);
                }
            }

            state.selectedIds.clear();
            showToast(`Deleted ${deletedCount} student record${deletedCount > 1 ? 's' : ''}.`, 'success');
        }

        await loadStudents();
    } catch (err) {
        console.error('Delete error:', err);
        showToast(`Delete failed: ${err.message}`, 'error');
    } finally {
        deleteConfirmBtn.disabled = false;
        deleteConfirmBtn.textContent = 'Delete Record';
        closeDeleteModal();
    }
}

// ─── CSV Export Functionality ─────────────────────────────────────────────────
function exportToCsv() {
    const listToExport = state.filtered.length > 0 ? state.filtered : state.students;
    if (listToExport.length === 0) {
        showToast('No student records to export.', 'info');
        return;
    }

    const headers = ['ID', 'Name', 'Email', 'Course', 'Age'];
    const rows = listToExport.map(s => [
        s.id,
        `"${String(s.name).replace(/"/g, '""')}"`,
        `"${String(s.email).replace(/"/g, '""')}"`,
        `"${String(s.course).replace(/"/g, '""')}"`,
        s.age
    ]);

    const csvContent = 'data:text/csv;charset=utf-8,' + [headers.join(','), ...rows.map(r => r.join(','))].join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `students_export_${new Date().toISOString().slice(0, 10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    showToast(`Exported ${listToExport.length} student records to CSV.`, 'success');
}

// ─── Modal Helpers ────────────────────────────────────────────────────────────
function openModal(overlay) {
    overlay.classList.add('open');
    document.body.style.overflow = 'hidden';
}

function closeModal(overlay) {
    overlay.classList.remove('open');
    document.body.style.overflow = '';
}

// ─── Toast System ─────────────────────────────────────────────────────────────
function showToast(message, type = 'info') {
    const icons = {
        success: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>`,
        error: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line></svg>`,
        info: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>`
    };

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
        <span class="toast-icon" aria-hidden="true">${icons[type] || icons.info}</span>
        <span class="toast-content">${escapeHtml(message)}</span>
        <button class="toast-close-btn" aria-label="Dismiss notification">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
        </button>
    `;

    toast.querySelector('.toast-close-btn').addEventListener('click', () => removeToast(toast));
    toastContainer.appendChild(toast);

    setTimeout(() => removeToast(toast), 4000);
}

function removeToast(toast) {
    if (!toast || !toast.parentNode) return;
    toast.classList.add('toast-out');
    toast.addEventListener('animationend', () => toast.remove(), { once: true });
}

// ─── Utility Helpers ──────────────────────────────────────────────────────────
function escapeHtml(str) {
    return String(str ?? '').replace(/[&<>"']/g, char => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    })[char]);
}

function getInitials(name) {
    if (!name) return '?';
    const parts = name.trim().split(/\s+/);
    if (parts.length === 1) return parts[0].substring(0, 2).toUpperCase();
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}
