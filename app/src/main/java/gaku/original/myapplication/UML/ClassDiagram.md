```mermaid
classDiagram


class ExpenseViewModel{
calendarDate
monthOffset
expense
_allExpenses

incrementMonth()
decrementMonth()
expenseの編集関数()
}

%%Firebase関連がまとめられる。ユーザーとか
class SharedViewModel{
firebaseAuth
currentUser
userId

setUserId()
signIn()
signUp()
signOut()
}
SharedViewModel ..> ExpenseViewModel :constructor injection

class ExpenseRepository{
Firebase.database.reference
listeners

clearlistners()
observeExpenses()
addUserInitialData()
fetchUserExpenses()
addExpense()
updateExpense()
removeExpense()
}
ExpenseRepository ..> ExpenseViewModel :constructor injection

class RealtimeDatabase{
Database
}
RealtimeDatabase <..> ExpenseRepository: 通信

````