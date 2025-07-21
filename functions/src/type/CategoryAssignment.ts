export type CategoryAssignment = {
  id?: string;
  categoryId: string;
  name: string;
  condition: string; // 後々入力制限する
  regex: Boolean;
  generatedTyep: String | null;
};

export type CategoryAssignmentData = {
  productName: Record<string, CategoryAssignment>;
  storeName: Record<string, CategoryAssignment>;
};
