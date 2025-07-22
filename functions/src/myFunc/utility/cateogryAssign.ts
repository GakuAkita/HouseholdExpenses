import { logger } from "firebase-functions";
import { AssignmentCondition } from "../../constants/AssignmentCondition";
import { Category } from "../../type/Category";
import { CategoryAssignment } from "../../type/CategoryAssignment";

/**
 * exact matchから優先的に検索し、
 * なかったら、containsを探す
 */
export const categoryAssign = (
  name: string,
  assignments: Record<string, CategoryAssignment>,
  categories: Record<string, Category>
): Category | null => {
  // まずは EXACT_MATCH を探す

  for (const assignment of Object.values(assignments)) {
    /**
     * ここでassignmentの諸々の値がちゃんと入っているかチェックする。
     * もし入っていなかったら、エラーログだけ残しておく。
     * 原因は、端末側で保存するタイプとfunctions側のタイプが合っていない。
     * デバッグ中にこれで事故った。
     *  */
    if (!assignment.name || !assignment.categoryId || !assignment.condition) {
      logger.error(`Invalid assignment found: ${JSON.stringify(assignment)}`);
      logger.error(`You need to check the assignment data.↑`);
    }

    if (assignment.condition === AssignmentCondition.EXACT_MATCH) {
      // 完全一致の場合
      if (name === assignment.name) {
        return categories[assignment.categoryId] ?? null;
      }
    }
  }

  // 次に CONTAINS を探す
  for (const assignment of Object.values(assignments)) {
    if (assignment.condition === AssignmentCondition.CONTAINS) {
      if (name.includes(assignment.name)) {
        return categories[assignment.categoryId] ?? null;
      }
    }
  }

  // どれにもマッチしなければ null
  return null;
};
