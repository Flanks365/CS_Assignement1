function selectAnswer(element, categoryName, answerId, questionId, autoplay, currentIndex) {
    console.log(element)
    console.log('Fetching answer for questionId:', questionId, 'and answerId:', answerId)
    fetch('getCorrectAnswer?questionId=' + questionId + '&answerId=' + answerId)
        .then(response => response.text())
        .then(result => {
            const buttons = element.parentNode.childNodes
            console.log(buttons)
            if (result === 'correct') {
                toggleButtonsDisabled(buttons)
                element.classList.add('correctButton')
                setTimeout(() => {
                    toggleButtonsDisabled(buttons)
                    element.classList.remove('correctButton')
                    window.location.href = "Quizpage?category_name=" + categoryName +
                        "&autoplay=" + autoplay + "&currentQuestionIndex=" + (currentIndex + 1)
                }, 1000)

            } else {
                toggleButtonsDisabled(buttons)
                element.classList.add('incorrectButton')
                setTimeout(() => {
                    toggleButtonsDisabled(buttons)
                    element.classList.remove('incorrectButton')
                }, 500)
            }
        })
        .catch(error => console.error('Error checking the answer:', error))
}

function toggleButtonsDisabled(buttons) {
    buttons.forEach(button => {
        button.disabled = !button.disabled
    })
}



window.addEventListener('load', () => {
    const autoplay = document.getElementById('autoplay').value;
    const correctAnswerID = document.getElementById('autoplayCorrectAnswer').value;
    const questionInfo = document.getElementById('questionInfo');
    if (autoplay == 'true') {
        let corrButton = document.getElementById(correctAnswerID);
        let secondsRemaining = 5
        questionInfo.innerHTML = "Time left: " + secondsRemaining
        const myInterval = setInterval(() => {
            secondsRemaining--
            questionInfo.innerHTML = "Time left: " + secondsRemaining
            if (!secondsRemaining)
                startAnimation()
        }, 1000)
        function startAnimation() {
            corrButton.classList.add('animation')
            clearInterval(myInterval)
            setTimeout(() => {
                corrButton.classList.remove('animation')
                corrButton.disabled = false
                corrButton.classList.add('correctButton')
                corrButton.click()
            }, 3500)
        }
    }
})





// console.log(document.getElementById('quoteOrBlob'))
// const media = document.getElementById('quoteOrBlob')

// let bt = document.createElement("button")
// bt.innerHTML = 'Accept'
// let bt2 = document.createElement("button")
// bt2.innerHTML = 'Now video'
// media.insertAdjacentElement('beforeend', bt)
// media.insertAdjacentElement('beforeend', bt2)

// const audio = media.querySelector('video')
// console.log(audio)
// // let bt = document.getElementById("bt");
// // console.log(audio);
// bt.addEventListener("click", ()=>{
//   audio.play();
// });
// bt2.addEventListener("click", changeMedia);
// const startPlaying = ()=>{
//   audio.removeEventListener('playing', startPlaying);
//   bt.classList.add("hide");
// //   audio.src = 'https://freesound.org/data/previews/475/475736_4397472-lq.mp3';
//   audio.play();
// }
// function changeMedia() {
//     audio.src = 'https://freesound.org/data/previews/475/475736_4397472-lq.mp3';
//     audio.play();
// }
// audio.addEventListener('playing', startPlaying);
// audio.addEventListener('error', ()=>{
//   console.log("error");
// });
