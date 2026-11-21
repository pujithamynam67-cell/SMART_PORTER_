/**
 * SMART PORTER - FRONTEND CLIENT CONTROLLER (RAPIDO / UBER STYLE)
 * Connects frontend UI to Java Backend REST API & MySQL Database.
 */

// Determine API Base URL dynamically (works on both /SmartPorter/ and root /)
const CONTEXT_PATH = window.location.pathname.startsWith('/SmartPorter') ? '/SmartPorter' : '';
const API_BASE = CONTEXT_PATH + '/api';

// Global Client State
let currentUser = null; // { userType: 'PASSENGER'|'PORTER'|'ADMIN', data: {...} }
let stations = [];
let currentActiveBooking = null;
let activePollInterval = null;

// Initialize on DOM Ready
document.addEventListener('DOMContentLoaded', () => {
    initDefaultSession();
    loadStations();
    setupEventListeners();
    setRating(5);
});

// Setup default demo user for instant out-of-the-box testing
function initDefaultSession() {
    const savedUser = localStorage.getItem('smart_porter_user');
    if (savedUser) {
        try {
            currentUser = JSON.parse(savedUser);
            updateNavProfile();
        } catch (e) {
            currentUser = null;
        }
    } else {
        // Default seed passenger for instant ease of use
        currentUser = {
            userType: 'PASSENGER',
            data: {
                passengerId: 1,
                fullName: 'Rajesh Sharma',
                phone: '9811002233',
                email: 'rajesh@example.com',
                isSeniorCitizen: false
            }
        };
        updateNavProfile();
    }
}

// Update Nav Profile Widget
function updateNavProfile() {
    const loggedInPill = document.getElementById('user-logged-in-pill');
    const guestActions = document.getElementById('user-guest-actions');
    const navName = document.getElementById('nav-user-name');
    const navRole = document.getElementById('nav-user-role');
    const navAvatar = document.getElementById('nav-user-avatar');

    if (currentUser) {
        loggedInPill.style.display = 'flex';
        guestActions.style.display = 'none';
        const name = currentUser.data.fullName || currentUser.data.username || 'User';
        navName.textContent = name;
        navRole.textContent = currentUser.userType;
        navAvatar.textContent = name.charAt(0).toUpperCase();

        if (currentUser.userType === 'PASSENGER') {
            loadPassengerHistory();
        } else if (currentUser.userType === 'PORTER') {
            loadPorterDashboard();
        } else if (currentUser.userType === 'ADMIN') {
            loadAdminData();
        }
    } else {
        loggedInPill.style.display = 'none';
        guestActions.style.display = 'block';
    }
}

function logoutUser() {
    currentUser = null;
    localStorage.removeItem('smart_porter_user');
    updateNavProfile();
    showToast('Signed out successfully.', 'info');
    switchView('passenger');
}

// View Navigation (Passenger / Porter / Admin)
function switchView(viewName) {
    document.querySelectorAll('.view-section').forEach(sec => sec.classList.remove('active'));
    document.querySelectorAll('.role-tab-btn').forEach(btn => btn.classList.remove('active'));

    const sec = document.getElementById('view-' + viewName);
    const tab = document.getElementById('tab-btn-' + viewName);
    if (sec) sec.classList.add('active');
    if (tab) tab.classList.add('active');

    if (viewName === 'admin') {
        loadAdminData();
        runDbmsDemo('JOINS');
    } else if (viewName === 'porter') {
        loadPorterDashboard();
    } else if (viewName === 'passenger') {
        loadPassengerHistory();
    }
}

// -------------------------------------------------------------
// STATIONS & FARE ESTIMATE
// -------------------------------------------------------------
async function loadStations() {
    try {
        const res = await fetch(`${API_BASE}/stations`);
        const json = await res.json();
        if (json.success && json.stations) {
            stations = json.stations;
            const select = document.getElementById('book-station-select');
            select.innerHTML = '';
            stations.forEach((st, idx) => {
                const opt = document.createElement('option');
                opt.value = st.stationId;
                opt.textContent = `${st.stationCode} - ${st.stationName} (${st.city})`;
                if (idx === 0) opt.selected = true;
                select.appendChild(opt);
            });
            calculateFareEstimate();
        }
    } catch (err) {
        console.error('Failed to load stations:', err);
    }
}

function selectLuggageType(type, el) {
    document.querySelectorAll('.luggage-option').forEach(o => o.classList.remove('selected'));
    el.classList.add('selected');
    document.getElementById('book-luggage-type').value = type;
    calculateFareEstimate();
}

function adjustCount(delta) {
    let count = parseInt(document.getElementById('book-quantity').value) || 1;
    count = Math.max(1, Math.min(10, count + delta));
    document.getElementById('book-quantity').value = count;
    document.getElementById('bag-count-display').textContent = count;
    calculateFareEstimate();
}

function syncWeight(val) {
    val = Math.max(1, Math.min(200, parseInt(val) || 1));
    document.getElementById('weight-slider').value = val;
    document.getElementById('book-weight').value = val;
    calculateFareEstimate();
}

async function calculateFareEstimate() {
    const stationId = parseInt(document.getElementById('book-station-select').value) || 1;
    const weightKg = parseFloat(document.getElementById('book-weight').value) || 25;
    const quantity = parseInt(document.getElementById('book-quantity').value) || 1;
    const isFragile = document.getElementById('book-fragile').checked;
    const isSenior = document.getElementById('book-senior').checked;

    try {
        const res = await fetch(`${API_BASE}/bookings/estimate-fare`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ stationId, weightKg, quantity, isFragile, isSeniorCitizen: isSenior })
        });
        const data = await res.json();
        if (data.success && data.estimate) {
            const est = data.estimate;
            document.getElementById('fare-base').textContent = `₹${est.baseFare}`;
            document.getElementById('fare-weight').textContent = `₹${est.weightCharge}`;
            document.getElementById('fare-handling').textContent = `₹${est.bagHandlingFee}`;

            const rowFragile = document.getElementById('row-fragile');
            if (isFragile) {
                rowFragile.style.display = 'flex';
                document.getElementById('fare-fragile').textContent = `₹${est.fragileSurcharge}`;
            } else {
                rowFragile.style.display = 'none';
            }

            const rowSenior = document.getElementById('row-senior');
            if (isSenior && est.seniorCitizenDiscount > 0) {
                rowSenior.style.display = 'flex';
                document.getElementById('fare-discount').textContent = `-₹${est.seniorCitizenDiscount}`;
            } else {
                rowSenior.style.display = 'none';
            }

            document.getElementById('fare-total').textContent = `₹${est.totalFare}`;
        }
    } catch (e) {
        console.error('Fare estimation error:', e);
    }
}

// -------------------------------------------------------------
// PASSENGER BOOKING WORKFLOW (RAPIDO / UBER ASSIGNMENT)
// -------------------------------------------------------------
async function handleCreateBooking(e) {
    e.preventDefault();

    if (!currentUser || currentUser.userType !== 'PASSENGER') {
        openAuthModal('passenger');
        showToast('Please sign in as passenger to book a porter.', 'error');
        return;
    }

    const submitBtn = document.getElementById('btn-request-porter');
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Finding Nearest Porter...';

    const payload = {
        passengerId: currentUser.data.passengerId,
        stationId: parseInt(document.getElementById('book-station-select').value),
        trainNumber: document.getElementById('book-train').value.trim(),
        trainName: 'Express Train',
        coachNumber: document.getElementById('book-coach').value.trim(),
        pickupPoint: document.getElementById('book-pickup').value,
        dropPoint: document.getElementById('book-drop').value,
        luggageType: document.getElementById('book-luggage-type').value,
        quantity: parseInt(document.getElementById('book-quantity').value),
        weightKg: parseFloat(document.getElementById('book-weight').value),
        isFragile: document.getElementById('book-fragile').checked,
        notes: 'Handled via Smart Porter webapp'
    };

    try {
        // Step 1: Create Booking via Stored Procedure
        const createRes = await fetch(`${API_BASE}/bookings/create`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const createData = await createRes.json();

        if (!createData.success) {
            showToast(createData.message || 'Failed to create booking', 'error');
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="fa-solid fa-bolt"></i> Request Smart Porter Now';
            return;
        }

        const booking = createData.booking;
        showToast(`Booking ${booking.bookingCode} Created! Matching porter...`, 'info');

        // Step 2: Auto Assign Porter (Uber-style matching)
        setTimeout(async () => {
            const assignRes = await fetch(`${API_BASE}/bookings/assign-porter`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ bookingId: booking.bookingId })
            });
            const assignData = await assignRes.json();

            if (assignData.success && assignData.match && assignData.match.assigned) {
                showToast(`Porter Assigned: ${assignData.match.porterName} (${assignData.match.badgeNumber})`, 'success');
            } else {
                showToast('Booking requested! Awaiting nearest porter assignment.', 'info');
            }

            // Start live tracker for passenger
            startLiveTracker(booking.bookingId);
            loadPassengerHistory();

            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="fa-solid fa-bolt"></i> Request Smart Porter Now';
        }, 1200);

    } catch (err) {
        console.error(err);
        showToast('Error connecting to server.', 'error');
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="fa-solid fa-bolt"></i> Request Smart Porter Now';
    }
}

// Real-Time Trip Tracker for Passenger
function startLiveTracker(bookingId) {
    if (activePollInterval) clearInterval(activePollInterval);

    const tripCard = document.getElementById('active-trip-card');
    tripCard.style.display = 'block';
    tripCard.scrollIntoView({ behavior: 'smooth' });

    async function poll() {
        try {
            const res = await fetch(`${API_BASE}/bookings/detail?bookingId=${bookingId}`);
            const data = await res.json();
            if (data.success && data.booking) {
                renderLiveTripDetails(data.booking);
                if (data.booking.serviceStatus === 'COMPLETED' || data.booking.serviceStatus === 'CANCELLED') {
                    clearInterval(activePollInterval);
                }
            }
        } catch (e) {
            console.error(e);
        }
    }

    poll();
    activePollInterval = setInterval(poll, 3000);
}

function renderLiveTripDetails(b) {
    currentActiveBooking = b;

    document.getElementById('active-trip-status-pill').textContent = b.serviceStatus;
    document.getElementById('active-trip-status-pill').className = `status-pill status-${b.serviceStatus}`;
    document.getElementById('active-booking-code').textContent = b.bookingCode;
    document.getElementById('active-pickup-display').textContent = b.pickupPoint;
    document.getElementById('active-drop-display').textContent = b.dropPoint;
    document.getElementById('active-luggage-display').textContent = `${b.quantity}x ${b.luggageType} (${b.weightKg} kg)`;

    // Steps
    const stepReq = document.getElementById('step-requested');
    const stepAcc = document.getElementById('step-accepted');
    const stepPick = document.getElementById('step-picked');
    const stepComp = document.getElementById('step-completed');

    [stepReq, stepAcc, stepPick, stepComp].forEach(s => s.className = 'status-step');

    if (b.serviceStatus === 'REQUESTED') {
        stepReq.className = 'status-step active';
    } else if (b.serviceStatus === 'ACCEPTED') {
        stepReq.className = 'status-step completed';
        stepAcc.className = 'status-step active';
    } else if (b.serviceStatus === 'PICKED_UP' || b.serviceStatus === 'IN_TRANSIT') {
        stepReq.className = 'status-step completed';
        stepAcc.className = 'status-step completed';
        stepPick.className = 'status-step active';
    } else if (b.serviceStatus === 'COMPLETED') {
        stepReq.className = 'status-step completed';
        stepAcc.className = 'status-step completed';
        stepPick.className = 'status-step completed';
        stepComp.className = 'status-step completed';
    }

    // Porter Info
    if (b.porterId) {
        document.getElementById('assigned-porter-box').style.display = 'flex';
        document.getElementById('active-porter-badge').textContent = b.porterBadge || 'CK-101';
        document.getElementById('active-porter-name').textContent = b.porterName || 'Licensed Porter';
        document.getElementById('active-porter-phone').textContent = b.porterPhone || '9876543210';
        document.getElementById('active-porter-rating').textContent = b.rating || '4.90';
    } else {
        document.getElementById('assigned-porter-box').style.display = 'none';
    }

    // Payment and Feedback buttons
    const payBtn = document.getElementById('btn-passenger-pay');
    const feedBtn = document.getElementById('btn-passenger-feedback');
    document.getElementById('pay-amount-display').textContent = `₹${b.totalFare}`;

    if (b.serviceStatus === 'IN_TRANSIT' || b.serviceStatus === 'PICKED_UP') {
        payBtn.style.display = 'block';
        feedBtn.style.display = 'none';
    } else if (b.serviceStatus === 'COMPLETED') {
        payBtn.style.display = 'none';
        if (!b.rating) {
            feedBtn.style.display = 'block';
        } else {
            feedBtn.style.display = 'none';
        }
    } else {
        payBtn.style.display = 'none';
        feedBtn.style.display = 'none';
    }
}

// Passenger History Table
async function loadPassengerHistory() {
    if (!currentUser || currentUser.userType !== 'PASSENGER') return;
    try {
        const res = await fetch(`${API_BASE}/bookings/passenger?passengerId=${currentUser.data.passengerId}`);
        const data = await res.json();
        const tbody = document.querySelector('#passenger-history-table tbody');
        tbody.innerHTML = '';

        if (data.success && data.history && data.history.length > 0) {
            data.history.forEach(r => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><strong>${r.bookingCode}</strong></td>
                    <td>${r.stationCode}</td>
                    <td>${r.porterBadge ? `${r.porterName} (${r.porterBadge})` : '<em style="color:var(--text-muted)">Unassigned</em>'}</td>
                    <td>₹${r.totalFare}</td>
                    <td><span class="status-pill status-${r.status}">${r.status}</span></td>
                `;
                tr.onclick = () => startLiveTracker(r.bookingId);
                tr.style.cursor = 'pointer';
                tbody.appendChild(tr);
            });
        } else {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; color:var(--text-muted)">No bookings yet. Book a porter above!</td></tr>';
        }
    } catch (e) {
        console.error(e);
    }
}

// -------------------------------------------------------------
// PORTER (COOLIE) CONSOLE WORKFLOW
// -------------------------------------------------------------
async function loadPorterDashboard() {
    if (!currentUser || currentUser.userType !== 'PORTER') {
        // Fallback demo porter
        currentUser = {
            userType: 'PORTER',
            data: {
                porterId: 1,
                badgeNumber: 'CK-101',
                fullName: 'Ramesh Kumar',
                phone: '9876543210',
                stationId: 1,
                stationName: 'New Delhi Railway Station (NDLS)',
                availabilityStatus: 'AVAILABLE',
                ratingAvg: 4.90,
                totalRides: 142,
                totalEarnings: 28400.00
            }
        };
        updateNavProfile();
    }

    const p = currentUser.data;
    document.getElementById('porter-profile-badge').textContent = p.badgeNumber || 'CK-101';
    document.getElementById('porter-profile-name').textContent = p.fullName;
    document.getElementById('porter-profile-station').textContent = p.stationName || 'NDLS';
    document.getElementById('porter-profile-rating').textContent = p.ratingAvg || '4.90';
    document.getElementById('porter-profile-trips').textContent = p.totalRides || '0';
    document.getElementById('porter-stat-earnings').textContent = `₹${p.totalEarnings || '0.00'}`;
    document.getElementById('porter-stat-trips').textContent = p.totalRides || '0';

    updatePorterDutyButton(p.availabilityStatus);
    loadPorterJobs();
}

function updatePorterDutyButton(status) {
    const btn = document.getElementById('btn-porter-toggle-duty');
    if (status === 'AVAILABLE') {
        btn.className = 'btn btn-success btn-sm';
        btn.innerHTML = '<i class="fa-solid fa-circle"></i> ONLINE (Available)';
    } else {
        btn.className = 'btn btn-danger btn-sm';
        btn.innerHTML = '<i class="fa-solid fa-circle-pause"></i> OFFLINE (On Break)';
    }
}

async function togglePorterDuty() {
    if (!currentUser || currentUser.userType !== 'PORTER') return;
    const curStatus = currentUser.data.availabilityStatus === 'AVAILABLE' ? 'OFFLINE' : 'AVAILABLE';
    try {
        const res = await fetch(`${API_BASE}/porters/status`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ porterId: currentUser.data.porterId, status: curStatus })
        });
        const data = await res.json();
        if (data.success) {
            currentUser.data.availabilityStatus = curStatus;
            updatePorterDutyButton(curStatus);
            showToast(`Status updated to ${curStatus}`, 'info');
        }
    } catch (e) {
        console.error(e);
    }
}

async function loadPorterJobs() {
    if (!currentUser || currentUser.userType !== 'PORTER') return;
    try {
        const res = await fetch(`${API_BASE}/bookings/porter?porterId=${currentUser.data.porterId}`);
        const data = await res.json();
        const tbody = document.querySelector('#porter-jobs-table tbody');
        tbody.innerHTML = '';

        let activeJobFound = null;

        if (data.success && data.jobs && data.jobs.length > 0) {
            data.jobs.forEach(j => {
                if (['ACCEPTED', 'PICKED_UP', 'IN_TRANSIT'].includes(j.status) && !activeJobFound) {
                    activeJobFound = j;
                }
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><strong>${j.bookingCode}</strong></td>
                    <td>${j.passengerName} (${j.passengerPhone})</td>
                    <td>${j.quantity}x ${j.luggageType} (${j.weightKg} kg)</td>
                    <td>₹${j.totalFare}</td>
                    <td><span class="status-pill status-${j.status}">${j.status}</span></td>
                `;
                tbody.appendChild(tr);
            });
        } else {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; color:var(--text-muted)">No jobs assigned yet.</td></tr>';
        }

        renderPorterActiveJob(activeJobFound);
    } catch (e) {
        console.error(e);
    }
}

function renderPorterActiveJob(job) {
    const box = document.getElementById('porter-active-job-box');
    const placeholder = document.getElementById('porter-no-job-placeholder');

    if (!job) {
        box.style.display = 'none';
        placeholder.style.display = 'block';
        return;
    }

    placeholder.style.display = 'none';
    box.style.display = 'block';

    document.getElementById('porter-job-code').textContent = job.bookingCode;
    document.getElementById('porter-job-passenger').textContent = job.passengerName;
    document.getElementById('porter-job-phone').textContent = job.passengerPhone;
    document.getElementById('porter-job-train').textContent = `${job.trainNumber || 'Train'} / Coach ${job.coachNumber || 'N/A'}`;
    document.getElementById('porter-job-pickup').textContent = job.pickupPoint;
    document.getElementById('porter-job-drop').textContent = job.dropPoint;
    document.getElementById('porter-job-luggage').textContent = `${job.quantity}x ${job.luggageType} (${job.weightKg} KG)`;
    document.getElementById('porter-job-fare').textContent = `₹${job.totalFare}`;

    const btnPicked = document.getElementById('btn-porter-step-picked');
    const btnTransit = document.getElementById('btn-porter-step-transit');
    const btnComplete = document.getElementById('btn-porter-step-complete');

    btnPicked.style.display = 'none';
    btnTransit.style.display = 'none';
    btnComplete.style.display = 'none';

    if (job.status === 'ACCEPTED') {
        btnPicked.style.display = 'block';
        btnPicked.onclick = () => advancePorterJobStatus('PICKED_UP', job.bookingId);
    } else if (job.status === 'PICKED_UP') {
        btnTransit.style.display = 'block';
        btnTransit.onclick = () => advancePorterJobStatus('IN_TRANSIT', job.bookingId);
    } else if (job.status === 'IN_TRANSIT') {
        btnComplete.style.display = 'block';
        btnComplete.onclick = () => advancePorterJobStatus('COMPLETED', job.bookingId);
    }
}

async function advancePorterJobStatus(status, bookingId) {
    try {
        if (status === 'COMPLETED') {
            const res = await fetch(`${API_BASE}/bookings/complete-pay`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ bookingId: bookingId, paymentMethod: 'CASH' })
            });
            const data = await res.json();
            if (data.success) {
                showToast('Luggage trip completed! Payment recorded and earnings credited.', 'success');
                loadPorterDashboard();
            }
        } else {
            const res = await fetch(`${API_BASE}/bookings/update-status`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ bookingId: bookingId, status: status })
            });
            const data = await res.json();
            if (data.success) {
                showToast(`Job updated: ${status}`, 'info');
                loadPorterJobs();
            }
        }
    } catch (e) {
        console.error(e);
    }
}

// -------------------------------------------------------------
// ADMIN ANALYTICS & DBMS CONCEPT DEMO
// -------------------------------------------------------------
async function loadAdminData() {
    try {
        // 1. Metrics
        const mRes = await fetch(`${API_BASE}/admin/metrics`);
        const mData = await mRes.json();
        if (mData.success && mData.metrics) {
            const m = mData.metrics;
            document.getElementById('admin-stat-bookings').textContent = m.totalBookings;
            document.getElementById('admin-stat-active').textContent = m.activeBookings;
            document.getElementById('admin-stat-porters').textContent = m.totalPorters;
            document.getElementById('admin-stat-avail-porters').textContent = m.availablePorters;
            document.getElementById('admin-stat-revenue').textContent = `₹${parseFloat(m.totalRevenue).toLocaleString()}`;
        }

        // 2. Station Analytics (View: vw_station_analytics)
        const aRes = await fetch(`${API_BASE}/admin/analytics`);
        const aData = await aRes.json();
        const tbodyStations = document.querySelector('#station-analytics-table tbody');
        tbodyStations.innerHTML = '';
        if (aData.success && aData.stationAnalytics) {
            aData.stationAnalytics.forEach(st => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><strong>${st.stationCode}</strong></td>
                    <td>${st.stationName}</td>
                    <td>${st.city}</td>
                    <td><span class="badge-tag" style="background:var(--success); color:#000;">${st.availablePorters}</span> / ${st.registeredPorters}</td>
                    <td>${st.totalBookings}</td>
                    <td>${st.luggageWeightKg} KG</td>
                    <td><strong style="color:var(--success);">₹${st.grossRevenue}</strong></td>
                    <td>₹${st.avgBookingValue}</td>
                `;
                tbodyStations.appendChild(tr);
            });
        }

        // 3. Porter Leaderboard (View: vw_porter_leaderboard)
        const lRes = await fetch(`${API_BASE}/admin/leaderboard`);
        const lData = await lRes.json();
        const tbodyLead = document.querySelector('#porter-leaderboard-table tbody');
        tbodyLead.innerHTML = '';
        if (lData.success && lData.leaderboard) {
            lData.leaderboard.forEach(po => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><span class="badge-tag" style="background:#ef4444; color:#fff;">${po.badgeNumber}</span></td>
                    <td><strong>${po.porterName}</strong></td>
                    <td>${po.stationCode}</td>
                    <td><i class="fa-solid fa-star" style="color:var(--primary);"></i> ${po.rating}</td>
                    <td>${po.totalRides}</td>
                    <td><strong style="color:var(--success);">₹${po.earnings}</strong></td>
                    <td><span class="status-pill status-${po.status}">${po.status}</span></td>
                `;
                tbodyLead.appendChild(tr);
            });
        }
    } catch (e) {
        console.error(e);
    }
}

// Interactive DBMS Concept Demonstration
async function runDbmsDemo(concept) {
    try {
        const res = await fetch(`${API_BASE}/admin/dbms-demo?concept=${concept}`);
        const data = await res.json();

        if (data.success && data.demo) {
            const demo = data.demo;
            document.getElementById('dbms-query-title').textContent = demo.title;
            document.getElementById('dbms-sql-code').textContent = demo.sqlExecuted;

            // Render columns
            const thead = document.getElementById('dbms-results-thead');
            thead.innerHTML = '';
            const trHead = document.createElement('tr');
            demo.columns.forEach(col => {
                const th = document.createElement('th');
                th.textContent = col;
                trHead.appendChild(th);
            });
            thead.appendChild(trHead);

            // Render rows
            const tbody = document.getElementById('dbms-results-tbody');
            tbody.innerHTML = '';
            if (demo.rows && demo.rows.length > 0) {
                demo.rows.forEach(row => {
                    const tr = document.createElement('tr');
                    demo.columns.forEach(col => {
                        const td = document.createElement('td');
                        td.textContent = row[col] !== null ? row[col] : '-';
                        tr.appendChild(td);
                    });
                    tbody.appendChild(tr);
                });
            } else {
                tbody.innerHTML = `<tr><td colspan="${demo.columns.length}" style="text-align:center; color:var(--text-muted)">Query executed successfully. 0 rows returned.</td></tr>`;
            }
        }
    } catch (e) {
        console.error('DBMS demo query failed:', e);
    }
}

// -------------------------------------------------------------
// PAYMENT & FEEDBACK MODALS
// -------------------------------------------------------------
function openPaymentModal() {
    if (!currentActiveBooking) return;
    document.getElementById('modal-pay-amount').textContent = `₹${currentActiveBooking.totalFare}`;
    document.getElementById('payment-modal').classList.add('open');
}

function closePaymentModal() {
    document.getElementById('payment-modal').classList.remove('open');
}

function selectPayMethod(method) {
    document.getElementById('selected-pay-method').value = method;
    ['upi', 'cash', 'card', 'wallet'].forEach(m => {
        const btn = document.getElementById(`pay-method-${m}`);
        if (m === method.toLowerCase()) {
            btn.style.borderColor = 'var(--primary)';
        } else {
            btn.style.borderColor = 'var(--border)';
        }
    });

    const upiBox = document.getElementById('upi-qr-box');
    if (method === 'UPI') {
        upiBox.style.display = 'block';
    } else {
        upiBox.style.display = 'none';
    }
}

async function executeTripPayment() {
    if (!currentActiveBooking) return;
    const method = document.getElementById('selected-pay-method').value;

    try {
        const res = await fetch(`${API_BASE}/bookings/complete-pay`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ bookingId: currentActiveBooking.bookingId, paymentMethod: method })
        });
        const data = await res.json();
        if (data.success) {
            closePaymentModal();
            showToast('Payment Successful! Thank you for using Smart Porter.', 'success');
            openFeedbackModal();
            loadPassengerHistory();
        } else {
            showToast(data.message || 'Payment failed', 'error');
        }
    } catch (e) {
        console.error(e);
        showToast('Error processing payment.', 'error');
    }
}

function openFeedbackModal() {
    document.getElementById('feedback-modal').classList.add('open');
}

function closeFeedbackModal() {
    document.getElementById('feedback-modal').classList.remove('open');
}

function setRating(val) {
    document.getElementById('feedback-stars').value = val;
    document.querySelectorAll('.star-rate').forEach(star => {
        const sVal = parseInt(star.getAttribute('data-val'));
        if (sVal <= val) {
            star.style.color = 'var(--primary)';
        } else {
            star.style.color = 'var(--border)';
        }
    });
    const descs = ['', '1 Star - Poor', '2 Stars - Fair', '3 Stars - Good', '4 Stars - Very Good', '5 Stars - Excellent Service'];
    document.getElementById('rating-desc').textContent = descs[val] || '';
}

async function submitPorterFeedback() {
    if (!currentActiveBooking) {
        closeFeedbackModal();
        return;
    }
    const rating = parseInt(document.getElementById('feedback-stars').value);
    const comments = document.getElementById('feedback-comments').value.trim();

    try {
        const res = await fetch(`${API_BASE}/feedback/submit`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                bookingId: currentActiveBooking.bookingId,
                passengerId: currentActiveBooking.passengerId,
                porterId: currentActiveBooking.porterId,
                rating: rating,
                comments: comments
            })
        });
        const data = await res.json();
        if (data.success) {
            closeFeedbackModal();
            showToast('Thank you! Porter rating updated via Database Trigger.', 'success');
            loadPassengerHistory();
        }
    } catch (e) {
        console.error(e);
    }
}

// -------------------------------------------------------------
// AUTH MODAL & LOGINS
// -------------------------------------------------------------
function openAuthModal(defaultTab = 'passenger') {
    document.getElementById('auth-modal').classList.add('open');
    switchAuthTab(defaultTab);
}

function closeAuthModal() {
    document.getElementById('auth-modal').classList.remove('open');
}

function switchAuthTab(tab) {
    ['passenger', 'porter', 'admin'].forEach(t => {
        document.getElementById(`auth-tab-${t.substring(0, 4)}`).classList.remove('active');
        document.getElementById(`auth-pane-${t}`).style.display = 'none';
    });
    document.getElementById(`auth-tab-${tab.substring(0, 4)}`).classList.add('active');
    document.getElementById(`auth-pane-${tab}`).style.display = 'block';
}

function togglePassRegister(show) {
    document.getElementById('passenger-login-form').style.display = show ? 'none' : 'block';
    document.getElementById('passenger-register-form').style.display = show ? 'block' : 'none';
}

async function handlePassengerLogin(e) {
    e.preventDefault();
    const id = document.getElementById('pass-login-id').value.trim();
    const pwd = document.getElementById('pass-login-pwd').value.trim();

    try {
        const res = await fetch(`${API_BASE}/auth/login-passenger`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ emailOrPhone: id, password: pwd })
        });
        const data = await res.json();
        if (data.success) {
            currentUser = { userType: 'PASSENGER', data: data.passenger };
            localStorage.setItem('smart_porter_user', JSON.stringify(currentUser));
            updateNavProfile();
            closeAuthModal();
            showToast(`Welcome back, ${data.passenger.fullName}!`, 'success');
            switchView('passenger');
        } else {
            showToast(data.message || 'Login failed', 'error');
        }
    } catch (err) {
        showToast('Connection error', 'error');
    }
}

async function handlePassengerRegister(e) {
    e.preventDefault();
    const name = document.getElementById('pass-reg-name').value.trim();
    const phone = document.getElementById('pass-reg-phone').value.trim();
    const email = document.getElementById('pass-reg-email').value.trim();
    const gender = document.getElementById('pass-reg-gender').value;
    const isSenior = document.getElementById('pass-reg-senior').checked;
    const pwd = document.getElementById('pass-reg-pwd').value.trim();

    try {
        const res = await fetch(`${API_BASE}/auth/register-passenger`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ fullName: name, phone, email, gender, isSeniorCitizen: isSenior, password: pwd })
        });
        const data = await res.json();
        if (data.success) {
            currentUser = { userType: 'PASSENGER', data: data.passenger };
            localStorage.setItem('smart_porter_user', JSON.stringify(currentUser));
            updateNavProfile();
            closeAuthModal();
            showToast(`Account created! Welcome, ${name}!`, 'success');
        } else {
            showToast(data.message || 'Registration failed', 'error');
        }
    } catch (err) {
        showToast('Connection error', 'error');
    }
}

async function handlePorterLogin(e) {
    e.preventDefault();
    const id = document.getElementById('port-login-id').value.trim();
    const pwd = document.getElementById('port-login-pwd').value.trim();

    try {
        const res = await fetch(`${API_BASE}/auth/login-porter`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ phoneOrBadge: id, password: pwd })
        });
        const data = await res.json();
        if (data.success) {
            currentUser = { userType: 'PORTER', data: data.porter };
            localStorage.setItem('smart_porter_user', JSON.stringify(currentUser));
            updateNavProfile();
            closeAuthModal();
            showToast(`Porter Logged In: ${data.porter.fullName} (${data.porter.badgeNumber})`, 'success');
            switchView('porter');
        } else {
            showToast(data.message || 'Invalid porter badge or password', 'error');
        }
    } catch (err) {
        showToast('Connection error', 'error');
    }
}

async function handleAdminLogin(e) {
    e.preventDefault();
    const username = document.getElementById('admin-login-id').value.trim();
    const pwd = document.getElementById('admin-login-pwd').value.trim();

    try {
        const res = await fetch(`${API_BASE}/auth/login-admin`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password: pwd })
        });
        const data = await res.json();
        if (data.success) {
            currentUser = { userType: 'ADMIN', data: { username: username } };
            localStorage.setItem('smart_porter_user', JSON.stringify(currentUser));
            updateNavProfile();
            closeAuthModal();
            showToast('Admin session started.', 'success');
            switchView('admin');
        } else {
            showToast(data.message || 'Invalid admin credentials', 'error');
        }
    } catch (err) {
        showToast('Connection error', 'error');
    }
}

// Toast Notifications
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;

    let icon = 'fa-circle-info';
    if (type === 'success') icon = 'fa-circle-check';
    if (type === 'error') icon = 'fa-triangle-exclamation';

    toast.innerHTML = `<i class="fa-solid ${icon}"></i> <span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

function setupEventListeners() {
    // ESC key closes modal
    window.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            closeAuthModal();
            closePaymentModal();
            closeFeedbackModal();
        }
    });
}
