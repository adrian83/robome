
function getCookie(name) {
    console.log('Getting cookie:', name);
    const value = `; ${document.cookie}`;
    console.log('Cookie value string:', value);
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
    return null;
}