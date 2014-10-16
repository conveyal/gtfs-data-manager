// return a class to highlight something that equals zero

module.exports = [
    'highlightZero',
    function (value) {
        return value == 0 ? 'bg-danger' : '';
    }
];
