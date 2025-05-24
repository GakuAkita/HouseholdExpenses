import { usersColRef } from "../../FirestoreService/FirestoreService";

export const getAllUserId = async (): Promise<string[]> => {
  const snapshot = await usersColRef.get();
  const allUsers: string[] = [];

  snapshot.forEach((doc) => {
    const userId = doc.id;
    allUsers.push(userId);
  });

  return allUsers;
};
