class Contact {
    constructor(id, name, phones, thumbnail, color) {
        this.id = id;
        this.name = name;
        this.phones = phones;
        this.thumbnail = thumbnail;
        this.color = color;
    }
}

class ContactPhone {
    constructor(number, type) {
        this.number = number;
        this.type = type;
    }
}
