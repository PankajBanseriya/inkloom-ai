/**
 * Tokenize and compare two strings for diff calculations
 */
export function computeWordDiff(original = '', updated = '') {
  const origWords = original.split(/(\s+)/);
  const updatedWords = updated.split(/(\s+)/);

  return {
    originalWordsCount: origWords.length,
    updatedWordsCount: updatedWords.length,
    isIdentical: original === updated,
  };
}
