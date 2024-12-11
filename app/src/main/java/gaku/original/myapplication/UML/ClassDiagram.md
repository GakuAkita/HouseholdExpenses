```mermaid
classDiagram


class ExpenseListViewModel{
calendarDate
monthOffset
_allExpenses

incrementMonth()
decrementMonth()
filterMonthExpenses()

readFromRemoteDB
deleteToRemoteDB
%%AddEditとこのdeleteがかぶる
}

class ExpenseAddEditViewModel{

ExpenseTmpを編集する関数()
addToRemoteDB
updateToRemoteDB
deleteToRemoteDB
}

class ExpenseTmp{
_expense
}

ExpenseTmp ..> ExpenseListViewModel :CI
ExpenseTmp ..> ExpenseAddEditViewModel :CI


class ExpenseRepository{
addUserInitialData()
fetchUserExpenses()
addExpense()
updateExpense()
removeExpense()
}
ExpenseRepository ..> ExpenseListViewModel :CI
ExpenseRepository ..> ExpenseAddEditViewModel :CI


class ExpenseListenerManager {
listeners

clearListeners()
addListeners()
}

class FirebaseReference {
Firebase.database.reference

getUserRef()
getExpenseRef()
getCategoryRef()
}

FirebaseReference ..> ExpenseListenerManager : CI
FirebaseReference ..> ExpenseRepository : CI
ExpenseListenerManager <..> RealtimeDatabase :リスナー管理

%%Firebase関連がまとめられる。ユーザーとか
class UserManageViewModel{
firebaseAuth
currentUser
userId

setUserId()
signIn()
signUp()
signOut()
}

UserManageViewModel ..> FirebaseReference : CI

class RealtimeDatabase{
Database
}
RealtimeDatabase <..> ExpenseRepository: CRUD

class FirebaseAuth{
Firebase Authentication
}
UserManageViewModel <..> FirebaseAuth: ユーザー管理

````