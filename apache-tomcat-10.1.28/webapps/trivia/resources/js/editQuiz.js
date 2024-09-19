console.log("js ran");

document.getElementById('content-quote').checked = true;
document.getElementById('new-question-form').style.display = 'none';

document.querySelectorAll('.file-input').forEach(input => input.style.display = 'none');
document.querySelectorAll('.quote-input').forEach(input => input.style.display = '');

const radioButtons = document.querySelectorAll('input[name="ContentType"]');
for (const radioButton of radioButtons) {
    radioButton.addEventListener('change', showContentInput);
}

document.getElementById('form-toggle').addEventListener('click', toggleForm);

document.querySelectorAll('.question-edit-container').forEach(container => {
    container.querySelector('.question-edit-form').style.display = 'none'
    container.querySelector('.radio-default').checked = true;
    console.log(container.attributes.questionid.value)
    console.log('question-edit-toggle-' + container.attributes.questionid.value)
    document.getElementById('question-edit-toggle-' + container.attributes.questionid.value).addEventListener('click', toggleEditForm)

});

function showContentInput(e) {
    console.log(e);
    console.log(e.target.value);
    console.log(this);
    const form = e.target.parentNode;
    const fileInput = form.querySelector('.file-input');
    const quoteInput = form.querySelector('.quote-input');
    console.log(form);
    if (e.target.value == "quote") {
        fileInput.style.display = 'none';
        quoteInput.style.display = '';
    } else {
        fileInput.style.display = '';
        fileInput.accept = e.target.value + '/*';
        quoteInput.style.display = 'none';
    }
}

function toggleForm(e) {
    console.log(e);
    if (e.target.innerHTML == "Create") {
        document.getElementById('new-question-form').style.display = '';
        e.target.innerHTML = "Cancel";
    } else {
        document.getElementById('new-question-form').style.display = 'none';
        e.target.innerHTML = "Create";
    }
}

function toggleEditForm(e) {
    console.log(e);
    const id = e.target.id.substring("question-edit-toggle-".length);
    console.log(id);
    const prefix = 'edit-form-'
    if (e.target.innerHTML == "Edit") {
        document.getElementById(prefix + id).style.display = '';
        e.target.innerHTML = "Cancel";
    } else {
        document.getElementById(prefix + id).style.display = 'none';
        e.target.innerHTML = "Edit";
    }
}