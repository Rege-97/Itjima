let onLogout: (() => void) | null = null;
export const setLogoutHandler = (fn: () => void) => {
  onLogout = fn;
};

export const triggerLogout = () => {
  if (onLogout) onLogout();
};
