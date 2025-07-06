// import { Firestore } from "firebase-admin/firestore";
// import { logger } from "firebase-functions";
// import {
//   FuncResultWithData,
//   FuncStatus
// } from "../../type/FuncStatus";
// import {
//   createRakutenPaySettingInstance,
//   RakutenPaySetting,
// } from "../../type/Mailbox";

// export class MailboxExtractionMailTypeSettingsService {
//   private db: Firestore;

//   constructor(db: Firestore) {
//     this.db = db;
//   }

//   /**
//    * メールボックス取得のためのパラメータのコレクション
//    */
//   private getUserMailboxExtractionMailTypeSettingsColRef(userId: string) {
//     return this.db
//       .collection("users")
//       .doc(userId)
//       .collection("mailbox_extraction_mail_type_settings");
//   }

//   private getUserMailboxExtractionRakutenPayDocRef(userId: string) {
//     const sampleRakuten = createRakutenPaySettingInstance({
//       enabled: true,
//     }); /* ここで指定されたdocumentNameがほしいだけに定義 */

//     return this.getUserMailboxExtractionMailTypeSettingsColRef(userId).doc(
//       sampleRakuten.nodeName
//     );
//   }

//   async getRakutenPaySetting(
//     userId: string
//   ): Promise<FuncResultWithData<RakutenPaySetting>> {
//     const docRef = this.getUserMailboxExtractionRakutenPayDocRef(userId);

//     try {
//       const snapShot = await docRef.get();
//       if (!snapShot.exists) {
//         return {
//           status: FuncStatus.EMPTY,
//           message: `MailboxExtraction token does not exist for user ${userId}.`,
//         };
//       }

//       const data: RakutenPaySetting = snapShot.data() as RakutenPaySetting;
//       if (!data) {
//         return {
//           status: FuncStatus.ERROR,
//           message: `data type doesn't match with expected type(RakutenPaySetting). user:${userId}`,
//         };
//       }

//       return {
//         status: FuncStatus.SUCCESS,
//         message: `getRakutenPaySetting Success`,
//         data: data,
//       };
//     } catch (e: any) {
//       return {
//         status: FuncStatus.ERROR,
//         message: `getRakutenPaySetting Failed: ${e.message}`,
//       };
//     }
//   }

//   /**
//    * これはlocalで試すときのみ使う!!
//    */
//   async setRakutenPaySetting(userId: string) {
//     const docRef = this.getUserMailboxExtractionRakutenPayDocRef(userId);
//     const sampleRakuten: RakutenPaySetting = createRakutenPaySettingInstance({
//       enabled: true,
//       storeCategoryAssignments: {
//         asdfsdfsa: {
//           id: "asdfsdfsa",
//           categoryId: "category1",
//           name: "ローソン",
//           condition: "contains",
//         },
//       },
//     });
//     try {
//       await docRef.set(sampleRakuten);
//     } catch (e: any) {
//       logger.error(`${e.message}`);
//     }
//   }

//   /**
//    * 基本的にfucntionからアップデートするのは新しく店を追加するときのみ。
//    */
//   // async updateRakutenShopCategoryAssignment(
//   //   userId: string,
//   //   newAssignments: Record<string, string>
//   // ): Promise<FuncResult> {
//   //   const docRef = this.getUserMailboxExtractionRakutenPayDocRef(userId);
//   //   try {
//   //     await docRef.update({
//   //       storeCategoryAssignments:
//   //         newAssignments /* ここベタ打ちなので怖いな、、 */,
//   //     });
//   //     return {
//   //       status: FuncStatus.SUCCESS,
//   //       message: `Successfully updated RakutenPayShopCategoryAssignments`,
//   //     };
//   //   } catch (e: any) {
//   //     return {
//   //       status: FuncStatus.ERROR,
//   //       message: `Failed to udpateRakutenShopCategoryAssignment:${e.message}`,
//   //     };
//   //   }
//   // }
// }
