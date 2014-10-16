// highlight start dates in the future in red
module.exports = [
    'getClassForStartDate',
    function (date) {
        if (new Date().getTime() > date >= 0)
            return '';
        else return 'bg-danger';
    }
];
