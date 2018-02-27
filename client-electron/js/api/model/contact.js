class Contact {
    constructor(awesomeSms, id, name, phones, thumbnail, color) {
        this.awesomeSms = awesomeSms;
        this.id = id;
        this.name = name;
        this.phones = phones;
        this.thumbnail = thumbnail;
        this.color = color;

    }

    getDisplayName() {
        if (this.name != null)
            return this.name;

        return formatPhone(this.phones[0].number);
    }

    getAbbreviation() {
        if (this.name == null)
            return "#";

        let names = this.name.split(' ');
        let abbr = names[0][0].toUpperCase();
        if (names.length > 1)
            abbr += names[names.length - 1][0].toUpperCase();
        return abbr;
    }
}

class ContactPhone {
    constructor(awesomeSms, number, type) {
        this.awesomeSms = awesomeSms;
        this.number = number;
        this.type = type;
    }
}
