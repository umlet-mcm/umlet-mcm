import { type ClassValue, clsx } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function prettyPrint(title: string) {
  return title
      .split("\n")                    // newlines in title are for visual purposes in UMLet(ino)
      .filter(l => !l.trim().startsWith('//')) // remove comment lines (inline-comments in title are not possible)
      .join(" ")
      .trim();                                 // remove (possible) extra space at the end
}