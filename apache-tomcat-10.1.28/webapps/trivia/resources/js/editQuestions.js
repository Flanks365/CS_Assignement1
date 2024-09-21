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
    const form = container.querySelector('.question-edit-form')
    form.style.display = 'none'
    const mediaType = container.querySelector('input[name="selectedContent"').value
    container.querySelector('#content-' + mediaType).checked = true;

    const fileInput = form.querySelector('.file-input');
    const quoteInput = form.querySelector('.quote-input');
    if (mediaType == "quote") {
        fileInput.style.display = 'none';
        quoteInput.style.display = '';
    } else {
        fileInput.style.display = '';
        fileInput.accept = mediaType + '/*';
        quoteInput.style.display = 'none';
    }

    document.getElementById('question-edit-toggle-' + container.attributes.questionid.value).addEventListener('click', toggleEditForm)

});

function showContentInput(e) {
    const form = e.target.parentNode;
    const fileInput = form.querySelector('.file-input');
    const quoteInput = form.querySelector('.quote-input');
    if (e.target.value == "quote") {
        fileInput.style.display = 'none';
        quoteInput.style.display = '';
        
        fileInput.required = false;
        quoteInput.required = true;
    } else {
        fileInput.style.display = '';
        fileInput.accept = e.target.value + '/*';
        quoteInput.style.display = 'none';

        fileInput.required = true;
        quoteInput.required = false;
    }
}


function toggleForm(e) {
    if (e.target.innerHTML == "Create") {
        document.getElementById('new-question-form').style.display = '';
        e.target.innerHTML = "Cancel";
    } else {
        document.getElementById('new-question-form').style.display = 'none';
        e.target.innerHTML = "Create";
    }
}

function toggleEditForm(e) {
    const id = e.target.id.substring("question-edit-toggle-".length);
    const prefix = 'edit-form-'
    if (e.target.innerHTML == "Edit") {
        document.getElementById(prefix + id).style.display = '';
        e.target.innerHTML = "Cancel";
    } else {
        document.getElementById(prefix + id).style.display = 'none';
        e.target.innerHTML = "Edit";
    }
}