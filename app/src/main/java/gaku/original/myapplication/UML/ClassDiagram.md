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

class CategoryRepository{
fetchAllCategory()
addCategory()
updateCategory()
removeCategory()
}

class DbListenerManager {
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

class onSignInOutViewModel{
onSignIn()
onSignOut()
}

onSignInOutViewModel ..> AuthManagerViewModel
ExpenseSharedViewModel ..> onSignInOutViewModel

DbListenerManager ..> ExpenseSharedViewModel :CI
ExpenseRepository ..> ExpenseSharedViewModel :CI
CategoryRepository ..> ExpenseSharedViewModel :CI
ExpenseSharedViewModel ..> ExpenseListViewModel :CI
ExpenseSharedViewModel ..> ExpenseAddEditViewModel :CI


class RealtimeDbReference {
Firebase.database.reference

getUserRef()
getExpenseRef()
getCategoryRef()
}

RealtimeDbReference ..> DbListenerManager : CI
RealtimeDbReference ..> ExpenseRepository : CI
RealtimeDbReference ..> CategoryRepository :CI
DbListenerManager <..> RealtimeDatabase :リスナー管理

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
%%キモいけどlistenerをサイン・アウト時にクリアするにはこうやって渡すしかないか～
%% DbListenerManager ..> UserManageViewModel :CI 
%% UserManageViewModel ..> RealtimeDbReference : CI

class RealtimeDatabase{
Database
}
RealtimeDatabase <..> ExpenseRepository: CRUD
RealtimeDatabase <..> CategoryRepository :CRUD

class FirebaseAuth{
Firebase Authentication
}
AuthManagerViewModel <..> FirebaseAuth: ユーザー管理

````