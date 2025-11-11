package functional.tests

class BootStrap {

    def init() {
    	Book.DB.drop()
    }

    def destroy() {
    }

}
