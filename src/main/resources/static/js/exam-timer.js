// This file drives the student exam page: the countdown timer,
// question navigation, saving answers, and submission.
// It expects two variables to already be defined by the page:
//   attemptId        (number)
//   secondsRemaining (number, calculated by the SERVER)

let currentIndex = 0;
const totalQuestions = document.querySelectorAll('.question-block').length;

function showQuestion(index) {
    document.querySelectorAll('.question-block').forEach((el) => {
        el.style.display = (Number(el.dataset.index) === index) ? '' : 'none';
    });
    document.querySelectorAll('.nav-btn').forEach((btn) => {
        btn.classList.remove('current-question');
        if (Number(btn.dataset.index) === index) {
            btn.classList.add('current-question');
        }
    });
    currentIndex = index;
}

document.getElementById('prevBtn').addEventListener('click', () => {
    if (currentIndex > 0) showQuestion(currentIndex - 1);
});

document.getElementById('nextBtn').addEventListener('click', () => {
    if (currentIndex < totalQuestions - 1) showQuestion(currentIndex + 1);
});

document.querySelectorAll('.nav-btn').forEach((btn) => {
    btn.addEventListener('click', () => showQuestion(Number(btn.dataset.index)));
});

// Whenever the student picks an answer, save it to the server
// immediately, and mark that question's navigator button green.
document.querySelectorAll('.answer-radio').forEach((radio) => {
    radio.addEventListener('change', () => {
        const questionId = radio.dataset.questionId;
        const selectedOption = radio.value;

        fetch(`/student/exam/${attemptId}/answer`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `questionId=${questionId}&selectedOption=${selectedOption}`
        });

        const navBtn = document.querySelector(`.question-block[data-index="${currentIndex}"]`)
            ? document.querySelectorAll('.nav-btn')[currentIndex]
            : null;
        if (navBtn) {
            navBtn.classList.remove('btn-outline-secondary');
            navBtn.classList.add('btn-success');
        }
    });
});

// ----- Submit confirmation -----
const confirmModalEl = document.getElementById('confirmModal');
const confirmModal = new bootstrap.Modal(confirmModalEl);

document.getElementById('submitBtn').addEventListener('click', () => {
    const answered = document.querySelectorAll('.nav-btn.btn-success').length;
    document.getElementById('answeredSummary').textContent =
        `You have answered ${answered} out of ${totalQuestions} questions.`;
    confirmModal.show();
});

document.getElementById('cancelSubmit').addEventListener('click', () => confirmModal.hide());

// ----- Countdown timer -----
const timerEl = document.getElementById('timerValue');

function formatTime(totalSeconds) {
    const m = Math.floor(totalSeconds / 60);
    const s = totalSeconds % 60;
    return String(m).padStart(2, '0') + ':' + String(s).padStart(2, '0');
}

function tick() {
    if (secondsRemaining <= 0) {
        timerEl.textContent = '00:00';
        // Time is up - submit automatically via the real form,
        // the server re-checks the time anyway, so this is safe.
        document.getElementById('submitForm').submit();
        return;
    }
    timerEl.textContent = formatTime(secondsRemaining);
    secondsRemaining--;
    setTimeout(tick, 1000);
}

showQuestion(0);
tick();
