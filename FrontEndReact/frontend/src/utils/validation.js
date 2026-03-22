export class CustomValidationUtils {

    static hasDangerousCharacters(input) {
        if (!input || typeof input !== 'string') return false;
        const dangerousPattern = /[<>"'&;]|script|javascript|onload|onerror/i;
        return dangerousPattern.test(input);
    }

    static isReservedLogin(login) {
        if (!login) return false;
        const reservedLogins = ["admin", "root", "system", "administrator"];
        const loginLower = login.toLowerCase();
        return reservedLogins.some(reserved => loginLower.includes(reserved));
    }

    static isValidUUID(uuid) {
        try {
            if (!uuid || typeof uuid !== 'string') return false;
            const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
            return uuidRegex.test(uuid);
        } catch (error) {
            return false;
        }
    }

    static isValidPhoneNumber(phone) {
        if (!phone) return false;
        const phonePattern = /^[+]?[0-9\s\-()]{9,20}$/;
        return phonePattern.test(phone);
    }

    static isValidEmail(email) {
        if (!email) return false;
        const emailPattern = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
        return emailPattern.test(email);
    }

    static isValidName(name) {
        if (!name) return false;
        return name.length >= 2 && name.length <= 100;
    }

    static isValidLogin(login) {
        if (!login) return false;
        const loginPattern = /^[a-zA-Z0-9_]+$/;
        return login.length >= 3 && login.length <= 50 && loginPattern.test(login);
    }

    static isValidAddress(address) {
        if (!address) return false;
        return address.length >= 5 && address.length <= 200;
    }
}