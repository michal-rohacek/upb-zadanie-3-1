document.getElementById("upload-public").addEventListener("click", function () {
    if (document.getElementById('generate').checked) {
        document.getElementById("public-key-input").style.display = "none"
    } else if (document.getElementById('upload-public').checked) {
        document.getElementById("public-key-input").style.display = "block"
    }
});
document.getElementById("generate").addEventListener("click", function () {
    if (document.getElementById('generate').checked) {
        document.getElementById("public-key-input").style.display = "none"
    } else if (document.getElementById('upload-public').checked) {
        document.getElementById("public-key-input").style.display = "block"
    }
});
document.getElementById("use-generated-key").addEventListener("click", function () {
    if (document.getElementById('use-generated-key').checked) {
        document.getElementById("public-key-input").style.display = "none"
    } else if (document.getElementById('upload-public').checked) {
        document.getElementById("public-key-input").style.display = "block"
    }
});