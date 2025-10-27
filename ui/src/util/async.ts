// Utility function to wrap synchronous calls in promises
export const wrapInPromise = <T>(fn: () => T): Promise<T> => {
  return new Promise((resolve) => {
    const result = fn();
    resolve(result);
  });
};
