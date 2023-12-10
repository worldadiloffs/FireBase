package uz.itschool.firebase.model

data class UserData(var name: String?, var uid: String?, var email: String?, var image: String?){

    constructor() : this(null, null, null, null)
}
