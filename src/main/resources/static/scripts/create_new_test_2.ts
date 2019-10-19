import {CanvasRenderer} from "./canvasrenderer";

document.addEventListener("DOMContentLoaded", function() {
    // Get canvas and set up renderer.
    const canvas: HTMLCanvasElement = document.querySelector("canvas");
    const renderer: CanvasRenderer = new CanvasRenderer(canvas);
    // Get the test ID of the test being created.
    const raw_url = window.location.href;
    const url = new URL(raw_url);
    const test_id: number = parseInt(url.searchParams.get("test_id"));
});