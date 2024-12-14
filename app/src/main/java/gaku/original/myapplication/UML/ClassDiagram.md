```mermaid
classDiagram

class ExpenseListViewModel{
calendarDate
monthOffset

incrementMonth()
decrementMonth()
filterMonthExpenses()
%%AddEditとこのdeleteがかぶる
}

class ExpenseAddEditViewModel{

ExpenseTmpを編集する関数()
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

class ListenerManager {
listeners

clearListeners()
addListeners()
}

class ExpenseSharedViewModel{
allExpenseList
将来的に数ヶ月分だけ取得とかの仕様にする↑

addExpense()
updateExpense()
deleteExpense()
}

ListenerManager ..> ExpenseSharedViewModel :CI
ExpenseRepository ..> ExpenseSharedViewModel :CI
ExpenseSharedViewModel ..> ExpenseListViewModel :CI
ExpenseSharedViewModel ..> ExpenseAddEditViewModel :CI

class RealtimeDbReference {
Firebase.database.reference

getUserRef()
getExpenseRef()
getCategoryRef()
}

RealtimeDbReference ..> ListenerManager : CI
RealtimeDbReference ..> ExpenseRepository : CI
ListenerManager <..> RealtimeDatabase :リスナー管理

%%Firebase関連がまとめられる。ユーザーとか
class UserInfoViewModel {
    currentUser
    userId
    isSignedIn
    getUserId()
}

class AuthManagerViewModel {
    signUpProcessFlag
    firstSignInFlag
    signIn()
    signUp()
    signOut()
}
UserInfoViewModel ..> AuthManagerViewModel :CI
UserInfoViewModel ..> RealtimeDbReference :CI
ListenerManager ..> AuthManagerViewModel :CI
%%キモいけどlistenerをサイン・アウト時にクリアするにはこうやって渡すしかないか～
%% ListenerManager ..> UserManageViewModel :CI 
%% UserManageViewModel ..> RealtimeDbReference : CI

class RealtimeDatabase{
Database
}
RealtimeDatabase <..> ExpenseRepository: CRUD

class FirebaseAuth{
Firebase Authentication
}
AuthManagerViewModel <..> FirebaseAuth: ユーザー管理

````