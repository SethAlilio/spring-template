import axios from "axios";

export class AuthService {

    baseUrl() {
        return window.location.protocol.concat("//").concat(window.location.hostname).concat(":8082");
    }

    baseApi() {
        return this.baseUrl() + "/api/auth"
    }

    signIn(username, password) {
        return axios.post(this.baseApi() + "/signin", {
            "username": username,
            "password": password
        })
    }

    signUp(account) {
        return axios.post(this.baseApi() + "/signup", account)
    }

    signOut() {
        return axios.post(this.baseApi() + "/signout")
    }

}
export default AuthService;