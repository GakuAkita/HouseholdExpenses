import { describe, expect, it } from "vitest";
import { AssignmentCondition } from "../../src/constants/AssignmentCondition";
import {
  assignCategoryById,
  assignCategoryFromAssignmentData,
  findCategoryFromAssignmentData,
} from "../../src/myFunc/utility/cateogryAssign";
import { Category } from "../../src/type/Category";
import { CategoryAssignment } from "../../src/type/CategoryAssignment";
import { Expense } from "../../src/type/Expense";

const categories: Record<string, Category> = {
  food: { id: "food", name: "食費", timestamp: 1, enabled: true },
  daily: { id: "daily", name: "日用品", timestamp: 1, enabled: true },
};

const assignment = (
  name: string,
  categoryId: string,
  condition: string
): CategoryAssignment => ({ name, categoryId, condition, regex: false, generatedTyep: null });

describe("findCategoryFromAssignmentData", () => {
  it("prefers an exact match over a contains match", () => {
    const assignments = {
      a: assignment("ローソン", "daily", AssignmentCondition.CONTAINS),
      b: assignment("ローソン 高松店", "food", AssignmentCondition.EXACT_MATCH),
    };
    expect(findCategoryFromAssignmentData("ローソン 高松店", assignments, categories)).toEqual(
      categories.food
    );
  });

  it("falls back to a contains match", () => {
    const assignments = { a: assignment("ローソン", "daily", AssignmentCondition.CONTAINS) };
    expect(findCategoryFromAssignmentData("ローソン 高松店", assignments, categories)).toEqual(
      categories.daily
    );
  });

  it("accepts the old lower-case conditions", () => {
    expect(
      findCategoryFromAssignmentData("A", { a: assignment("A", "food", "exact_match") }, categories)
    ).toEqual(categories.food);
    expect(
      findCategoryFromAssignmentData("AB", { a: assignment("A", "daily", "contains") }, categories)
    ).toEqual(categories.daily);
  });

  it("returns null when nothing matches or the category doesn't exist", () => {
    const assignments = { a: assignment("ローソン", "deleted", AssignmentCondition.EXACT_MATCH) };
    expect(findCategoryFromAssignmentData("セブン", assignments, categories)).toBeNull();
    expect(findCategoryFromAssignmentData("ローソン", assignments, categories)).toBeNull();
  });
});

describe("assignCategoryFromAssignmentData", () => {
  const expense: Expense = { amount: 500, storeName: "ローソン" };

  it("sets the found category without changing the input", () => {
    const assignments = { a: assignment("ローソン", "food", AssignmentCondition.EXACT_MATCH) };
    const result = assignCategoryFromAssignmentData(expense, "ローソン", assignments, categories);
    expect(result).toEqual({ ...expense, category: categories.food });
    expect(expense.category).toBeUndefined();
  });

  it("returns the same expense when nothing matches", () => {
    expect(assignCategoryFromAssignmentData(expense, "セブン", {}, categories)).toBe(expense);
  });
});

describe("assignCategoryById", () => {
  const expense: Expense = { amount: 500 };

  it("sets the category with the given id", () => {
    expect(assignCategoryById(expense, "food", categories)).toEqual({
      ...expense,
      category: categories.food,
    });
  });

  it("returns the same expense without a category id", () => {
    expect(assignCategoryById(expense, undefined, categories)).toBe(expense);
  });

  it("sets category to undefined when the id doesn't exist", () => {
    /* Current behaviour: the property is present but undefined. */
    const result = assignCategoryById(expense, "deleted", categories);
    expect(result).toHaveProperty("category", undefined);
  });
});
